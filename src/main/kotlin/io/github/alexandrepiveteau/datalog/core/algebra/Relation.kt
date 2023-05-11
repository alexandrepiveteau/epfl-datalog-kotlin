package io.github.alexandrepiveteau.datalog.core.algebra

import io.github.alexandrepiveteau.datalog.core.Atom
import io.github.alexandrepiveteau.datalog.core.AtomList
import io.github.alexandrepiveteau.datalog.core.asAtomList
import io.github.alexandrepiveteau.datalog.core.plus

/**
 * A [Relation] contains a set of tuples as [AtomList]s and has a certain arity.
 *
 * @property arity the number of atoms in each row of the relation.
 * @property tuples the sequence of [AtomList]s in the relation.
 *
 * TODO : Perform operations using a block-iterator-based model.
 */
data class Relation(val arity: Int, val tuples: Set<AtomList>) {

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
fun Relation.Companion.empty(arity: Int): Relation {
  return Relation(arity, emptySet())
}

/** Iterates over all the rows in the relation, applying [f] to each of them. */
inline fun Relation.forEach(f: (AtomList) -> Unit) = tuples.forEach(f)

/**
 * Performs a selection on the relation, and returns the result. The arity of the resulting relation
 * is the same as the arity of the original relation.
 */
fun Relation.select(
    f: (AtomList) -> Boolean,
): Relation = buildRelation(arity) { forEach { if (f(it)) yield(it) } }

/**
 * Performs a natural join between this relation and [other], and returns the result. The arity of
 * the resulting relation is the sum of the arity of both relations.
 */
fun Relation.join(other: Relation): Relation {
  return buildRelation(arity + other.arity) { forEach { a -> other.forEach { b -> yield(a + b) } } }
}

/**
 * Performs a natural join between this [Relation] and all the [others], and returns the result. The
 * arity of the resulting relation is the sum of the arity of all relations.
 */
fun Relation.join(others: Iterable<Relation>): Relation {
  var result = this
  for (other in others) result = result.join(other)
  return result
}

/** @see Relation.join */
fun Iterable<Relation>.join(): Relation {
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
fun Relation.union(other: Relation): Relation {
  require(other.arity == arity) { "The arity of the relations must be the same." }
  return buildRelation(arity) {
    forEach { yield(it) }
    other.forEach { yield(it) }
  }
}

/** Filters duplicate rows in the relation, and returns the result. */
fun Relation.distinct(): Relation {
  return buildRelation(arity) {
    val seen = mutableSetOf<AtomList>()
    forEach { if (seen.add(it)) yield(it) }
  }
}

/**
 * The kind of outputs which are possible for a [Relation] projection.
 *
 * @see Relation.project
 */
sealed interface Column {

  /** Projects a constant value for all rows. */
  data class Constant(val value: Atom) : Column

  /** Projects a value from the row at the given index. */
  data class Index(val index: Int) : Column
}

/**
 * Applies the given [projection] to the relation, and returns the result. The arity of the
 * resulting relation is the same as the number of columns in the projection.
 */
fun Relation.project(projection: List<Column>): Relation {
  return buildRelation(projection.size) {
    forEach { atom ->
      yield(
          projection
              .map {
                when (it) {
                  is Column.Constant -> it.value
                  is Column.Index -> atom[it.index]
                }
              }
              .asAtomList(),
      )
    }
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
fun buildRelation(
    arity: Int,
    builder: suspend SequenceScope<AtomList>.() -> Unit,
): Relation = Relation(arity, sequence { builder() }.toSet())
