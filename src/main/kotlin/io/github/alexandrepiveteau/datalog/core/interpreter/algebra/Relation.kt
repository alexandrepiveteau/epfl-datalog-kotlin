package io.github.alexandrepiveteau.datalog.core.interpreter.algebra

import io.github.alexandrepiveteau.datalog.core.*
import io.github.alexandrepiveteau.datalog.core.RuleBuilder.Aggregate
import io.github.alexandrepiveteau.datalog.core.interpreter.algebra.Column.Index

/**
 * A [Relation] contains a set of tuples as [AtomList]s and has a certain arity.
 *
 * @property arity the number of atoms in each row of the relation.
 * @property tuples the sequence of [AtomList]s in the relation.
 *
 * TODO : Perform operations using a block-iterator-based model.
 */
internal data class Relation(val arity: Int, val tuples: Set<AtomList>) {

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

/** Returns an empty [Relation] of the given arity. The resulting relation will have no rows. */
internal fun Relation.Companion.empty(arity: Int): Relation {
  return Relation(arity, emptySet())
}

/**
 * Returns a [Relation] with [arity] columns, which contains all value combinations from the given
 * domain [values]s.
 */
internal fun Relation.Companion.domain(arity: Int, values: Sequence<Atom>): Relation {
  val column = buildRelation(1) { for (value in values) yield(arrayOf(value).asAtomList()) }
  var result = if (arity == 0) return empty(0) else column
  for (i in 1 until arity) result = result.join(column)
  return result
}

/** Iterates over all the rows in the relation, applying [f] to each of them. */
internal inline fun Relation.forEach(f: (AtomList) -> Unit) = tuples.forEach(f)

/** Returns the value of the [Column] in the [AtomList]. */
private fun AtomList.value(column: Column): Atom =
    when (column) {
      is Column.Constant -> column.value
      is Index -> this[column.index]
    }

/**
 * Performs a selection on the relation, and returns the result. The arity of the resulting relation
 * is the same as the arity of the original relation.
 */
internal fun Relation.select(selection: Set<Set<Column>>): Relation {
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
 */
internal fun Relation.join(other: Relation): Relation {
  return buildRelation(arity + other.arity) { forEach { a -> other.forEach { b -> yield(a + b) } } }
}

/**
 * Performs a natural join between this [Relation] and all the [others], and returns the result. The
 * arity of the resulting relation is the sum of the arity of all relations.
 */
internal fun Relation.join(others: Iterable<Relation>): Relation {
  var result = this
  for (other in others) result = result.join(other)
  return result
}

/** @see Relation.join */
internal fun Iterable<Relation>.join(): Relation {
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
 */
internal fun Relation.union(other: Relation): Relation {
  require(other.arity == arity) { "The arity of the relations must be the same." }
  return buildRelation(arity) {
    forEach { yield(it) }
    other.forEach { yield(it) }
  }
}

/** Filters duplicate rows in the relation, and returns the result. */
internal fun Relation.distinct(): Relation {
  return buildRelation(arity) {
    val seen = mutableSetOf<AtomList>()
    forEach { if (seen.add(it)) yield(it) }
  }
}

/**
 * Subtracts [other] from this relation, and returns the result. The arity of the resulting relation
 * is the same as the arity of both relations.
 */
internal operator fun Relation.minus(other: Relation): Relation {
  require(other.arity == arity) { "The arity of the relations must be the same." }
  return buildRelation(arity) { forEach { if (it !in other.tuples) yield(it) } }
}

/**
 * Applies the given [projection] to the relation, and returns the result. The arity of the
 * resulting relation is the same as the number of columns in the projection.
 */
internal fun Relation.project(projection: List<Column>): Relation {
  return buildRelation(projection.size) {
    forEach { atom -> yield(projection.map { atom.value(it) }.asAtomList()) }
  }
}

/**
 * Returns the [value] of the [Atom] for the given [AggregationColumn], considering that the
 * [indices] is used for aggregation positions in the original [AtomList].
 *
 * @param column the [AggregationColumn] to get the value for.
 * @param indices the [Index]es of the [AtomList] in the original relation.
 * @param aggregate the [Aggregate] to apply to the values in the same set.
 * @param domain the [Domain] of the [AtomList]s.
 */
private fun AtomList.value(
    column: AggregationColumn,
    indices: Set<Index>,
    aggregate: Aggregate,
    domain: Domain,
): Atom {
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
 * Merges the [first] and [second] [AtomList] into a new [AtomList] with the given [aggregate] and
 * [columns].
 *
 * The [columns] have already been applied, and should therefore be considered as the final columns
 * of the resulting [AtomList]. Only the [AggregationColumn.Aggregate]s in the [columns] will be
 * merged.
 *
 * @param first the first [AtomList] to merge.
 * @param second the second [AtomList] to merge.
 * @param aggregate the [Aggregate] to apply to the values in the same set.
 * @param columns the [AggregationColumn]s to respect in the projected values.
 * @return the [AtomList] resulting from the merge.
 */
private fun Domain.merge(
    first: AtomList,
    second: AtomList,
    aggregate: Aggregate,
    columns: List<AggregationColumn>,
): AtomList {
  return columns
      .mapIndexed { index, column ->
        val x = first[index]
        val y = second[index]
        when (column) {
          is AggregationColumn.Column -> x
          is AggregationColumn.Aggregate -> with(aggregate) { combine(x, y) }
        }
      }
      .asAtomList()
}

/**
 * Aggregates the given [Relation] into a new [Relation] with the given [projection].
 *
 * @param projection the [AggregationColumn]s to respect in the projected values.
 * @param same the set of [Index] which serve as the key for the aggregation.
 * @param domain the [Domain] of the [Relation].
 * @param aggregate the [Aggregate] to apply to the values in the same set.
 * @param indices the [Index] of the columns to aggregate.
 */
internal fun Relation.aggregate(
    projection: List<AggregationColumn>,
    same: Set<Index>,
    domain: Domain,
    aggregate: Aggregate,
    indices: Set<Index>,
): Relation {
  require(indices.all { it !in same }) { "The indices must not be in the same set." }
  require(
      projection
          .filterIsInstance<AggregationColumn.Column>()
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
    val result = mutableMapOf<AtomList, AtomList>()
    distinct().forEach { atom ->
      val key = same.map { atom[it.index] }.asAtomList()
      val existing = result[key]
      val value = projection.map { atom.value(it, indices, aggregate, domain) }.asAtomList()
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
 * function is a suspending function that can be used to yield new [AtomList]s.
 *
 * @param arity the arity of the relation.
 * @param builder the suspending function that can be used to yield new [AtomList]s.
 * @return a new [Relation] with the given [arity], and the given [builder] function.
 */
internal fun buildRelation(
    arity: Int,
    builder: suspend SequenceScope<AtomList>.() -> Unit,
): Relation = Relation(arity, sequence { builder() }.toSet())
