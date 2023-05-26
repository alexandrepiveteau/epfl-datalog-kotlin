package io.github.alexandrepiveteau.datalog.core.interpreter.algebra

import io.github.alexandrepiveteau.datalog.core.RuleBuilder.Aggregate
import io.github.alexandrepiveteau.datalog.core.interpreter.algebra.Column.Index
import io.github.alexandrepiveteau.datalog.dsl.Domain
import io.github.alexandrepiveteau.datalog.dsl.Value

/**
 * A [Relation] contains a set of tuples as [List]s of [Value]s and has a certain arity.
 *
 * TODO : Perform operations using a block-iterator-based model.
 *
 * @param T the type of the elements in the relation.
 * @property arity the number of atoms in each row of the relation.
 * @property tuples the sequence of [List] of [Value]s in the relation.
 */
internal data class Relation<out T>(val arity: Int, val tuples: Set<List<Value<T>>>) {

  override fun toString(): String = buildString {
    append("Relation(arity=")
    append(arity)
    append(") {")
    var first = true
    this@Relation.forEach { row ->
      if (!first) append(", ")
      first = false
      append(row)
    }
    append("}")
  }

  companion object
}

/**
 * Returns an empty [Relation] of the given arity. The resulting relation will have no rows.
 *
 * @param T the type of the elements in the relation.
 */
internal fun <T> Relation.Companion.empty(arity: Int): Relation<T> {
  return Relation(arity, emptySet())
}

/**
 * Returns a [Relation] with [arity] columns, which contains all value combinations from the given
 * domain [values]s.
 *
 * @param T the type of the elements in the relation.
 */
internal fun <T> Relation.Companion.domain(arity: Int, values: Sequence<Value<T>>): Relation<T> {
  val column = Relation(arity = 1, tuples = values.mapTo(mutableSetOf(), ::listOf))
  var result = if (arity == 0) return empty(0) else column
  for (i in 1 until arity) result = result.join(column)
  return result
}

/**
 * Iterates over all the rows in the relation, applying [f] to each of them.
 *
 * @param T the type of the elements in the relation.
 */
internal inline fun <T> Relation<T>.forEach(f: (List<Value<T>>) -> Unit) = tuples.forEach(f)

/**
 * Returns the value of the [Column] in the [List] of values.
 *
 * @param T the type of the elements in the relation.
 */
private fun <T> List<Value<T>>.value(column: Column<T>): Value<T> =
    when (column) {
      is Column.Constant -> column.value
      is Index -> this[column.index]
    }

/**
 * Performs a selection on the relation, and returns the result. The arity of the resulting relation
 * is the same as the arity of the original relation.
 *
 * @param T the type of the elements in the relation.
 */
internal fun <T> Relation<T>.select(selection: Set<Set<Column<T>>>): Relation<T> {
  val list = selection.map { it.toList() }
  return buildRelation(arity) {
    forEach { row ->
      val insert = list.all { g -> g.all { c -> row.value(c) == row.value(g.first()) } }
      if (insert) yield(row)
    }
  }
}

/**
 * Performs a natural join between this relation and [other], and returns the result. The arity of
 * the resulting relation is the sum of the arity of both relations.
 *
 * @param T the type of the elements in the relation.
 */
internal fun <T> Relation<T>.join(other: Relation<T>): Relation<T> {
  return buildRelation(arity + other.arity) { forEach { a -> other.forEach { b -> yield(a + b) } } }
}

/**
 * Performs a natural join between this [Relation] and all the [others], and returns the result. The
 * arity of the resulting relation is the sum of the arity of all relations.
 *
 * @param T the type of the elements in the relation.
 */
internal fun <T> Relation<T>.join(others: Iterable<Relation<T>>): Relation<T> {
  var result = this
  for (other in others) result = result.join(other)
  return result
}

/** @see Relation.join */
internal fun <T> Iterable<Relation<T>>.join(): Relation<T> {
  val iterator = iterator().apply { require(hasNext()) { "The iterable must not be empty." } }
  var result = iterator.next()
  while (iterator.hasNext()) {
    result = result.join(iterator.next())
  }
  return result
}

/**
 * Performs a union between this relation and [other], and returns the result. The arity of the
 * resulting relation is the same as the arity of both relations.
 *
 * @param T the type of the elements in the relation.
 */
internal fun <T> Relation<T>.union(other: Relation<T>): Relation<T> {
  require(other.arity == arity) { "The arity of the relations must be the same." }
  return buildRelation(arity) {
    forEach { yield(it) }
    other.forEach { yield(it) }
  }
}

/**
 * Filters duplicate rows in the relation, and returns the result.
 *
 * @param T the type of the elements in the relation.
 */
internal fun <T> Relation<T>.distinct(): Relation<T> {
  return buildRelation(arity) {
    val seen = mutableSetOf<List<Value<T>>>()
    forEach { if (seen.add(it)) yield(it) }
  }
}

/**
 * Subtracts [other] from this relation, and returns the result. The arity of the resulting relation
 * is the same as the arity of both relations.
 *
 * @param T the type of the elements in the relation.
 */
internal operator fun <T> Relation<T>.minus(other: Relation<T>): Relation<T> {
  require(other.arity == arity) { "The arity of the relations must be the same." }
  return buildRelation(arity) { forEach { if (it !in other.tuples) yield(it) } }
}

/**
 * Applies the given [projection] to the relation, and returns the result. The arity of the
 * resulting relation is the same as the number of columns in the projection.
 *
 * @param T the type of the elements in the relation.
 */
internal fun <T> Relation<T>.project(projection: List<Column<T>>): Relation<T> {
  return buildRelation(projection.size) {
    forEach { atom -> yield(projection.map { atom.value(it) }) }
  }
}

/**
 * Returns the [value] of the [Value]s for the given [AggregationColumn], considering that the
 * [indices] is used for aggregation positions in the original [List] of [Value]s.
 *
 * @param T the type of the elements in the relation.
 * @param column the [AggregationColumn] to get the value for.
 * @param indices the [Index]es of the [List] of [Value]s in the original relation.
 * @param aggregate the [Aggregate] to apply to the values in the same set.
 * @param domain the [Domain] of the [List] of [Value]s.
 */
private fun <T> List<Value<T>>.value(
    column: AggregationColumn<T>,
    indices: Set<Index>,
    aggregate: Aggregate,
    domain: Domain<T>,
): Value<T> {
  return when (column) {
    is AggregationColumn.Column -> value(column.column)
    is AggregationColumn.Aggregate ->
        toList()
            .asSequence()
            .mapIndexed { index, _ -> Index(index) }
            .filter { it in indices }
            .toSet()
            .let { with(aggregate) { domain.transform(it, this@value) } }
  }
}

/**
 * Merges the [first] and [second] [List] of [Value]s into a new [List] of [Value]s with the given
 * [aggregate] and [columns].
 *
 * The [columns] have already been applied, and should therefore be considered as the final columns
 * of the resulting [List] of [Value]s. Only the [AggregationColumn.Aggregate]s in the [columns]
 * will be merged.
 *
 * @param T the type of the elements in the relation.
 * @param first the first [List] of [Value]s to merge.
 * @param second the second [List] of [Value]s to merge.
 * @param aggregate the [Aggregate] to apply to the values in the same set.
 * @param columns the [AggregationColumn]s to respect in the projected values.
 * @return the [List] of [Value]s resulting from the merge.
 */
private fun <T> Domain<T>.merge(
    first: List<Value<T>>,
    second: List<Value<T>>,
    aggregate: Aggregate,
    columns: List<AggregationColumn<T>>,
): List<Value<T>> {
  return columns.mapIndexed { index, column ->
    val x = first[index]
    val y = second[index]
    when (column) {
      is AggregationColumn.Column -> x
      is AggregationColumn.Aggregate -> with(aggregate) { combine(x, y) }
    }
  }
}

/**
 * Aggregates the given [Relation] into a new [Relation] with the given [projection].
 *
 * @param T the type of the elements in the relation.
 * @param projection the [AggregationColumn]s to respect in the projected values.
 * @param same the set of [Index] which serve as the key for the aggregation.
 * @param domain the [Domain] of the [Relation].
 * @param aggregate the [Aggregate] to apply to the values in the same set.
 * @param indices the [Index] of the columns to aggregate.
 */
internal fun <T> Relation<T>.aggregate(
    projection: List<AggregationColumn<T>>,
    same: Set<Index>,
    domain: Domain<T>,
    aggregate: Aggregate,
    indices: Set<Index>,
): Relation<T> {
  require(indices.all { it !in same }) { "The indices must not be in the same set." }
  require(
      projection
          .filterIsInstance<AggregationColumn.Column<T>>()
          .map { it.column }
          .filterIsInstance<Index>()
          .all { it in same },
  ) {
    "All columns in the projection must be in the same set."
  }
  return buildRelation(projection.size) {
    // A map of the values in the same set to the values in the projection, for each row. Multiple
    // rows will eventually map to the same value, and the aggregate function will be applied to
    // merge them.
    val result = mutableMapOf<List<Value<T>>, List<Value<T>>>()
    distinct().forEach { atom ->
      val key = same.map { atom[it.index] }
      val existing = result[key]
      val value = projection.map { atom.value(it, indices, aggregate, domain) }
      val updated =
          if (existing == null) value
          else with(domain) { merge(existing, value, aggregate, projection) }
      result[key] = updated
    }
    result.forEach { (_, value) -> yield(value) }
  }
}

/**
 * Builds a new [Relation] with the given [arity], and the given [builder] function. The [builder]
 * function is a suspending function that can be used to yield new [List] of [Value]s.
 *
 * @param T the type of the elements in the relation.
 * @param arity the arity of the relation.
 * @param builder the suspending function that can be used to yield new [List]s of [Value]s.
 * @return a new [Relation] with the given [arity], and the given [builder] function.
 */
internal fun <T> buildRelation(
    arity: Int,
    builder: suspend SequenceScope<List<Value<T>>>.() -> Unit,
): Relation<T> = Relation(arity, sequence { builder() }.toSet())
