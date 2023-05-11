package io.github.alexandrepiveteau.datalog.core.algebra

import io.github.alexandrepiveteau.datalog.core.Atom
import io.github.alexandrepiveteau.datalog.core.AtomList
import io.github.alexandrepiveteau.datalog.core.asAtomList
import io.github.alexandrepiveteau.datalog.core.plus

/**
 * An interface representing a [Relation], which contains a set of rows as [AtomList]s, and has a
 * certain arity.
 *
 * TODO : Perform operations using a block-iterator-based model.
 */
abstract class Relation {

  /** Returns the number of atoms in each row of the relation. */
  abstract val arity: Int

  /** Returns an [Iterator] which can be used to traverse the [Relation]. */
  abstract fun iterator(): Iterator<AtomList>

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
  return object : Relation() {
    override val arity: Int = arity
    override fun iterator(): Iterator<AtomList> = emptyList<AtomList>().iterator()
  }
}

/** Iterates over all the rows in the relation, applying [f] to each of them. */
inline fun Relation.forEach(f: (AtomList) -> Unit) {
  val iterator = iterator()
  while (iterator.hasNext()) f(iterator.next())
}

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

/** Returns true if the two [Relation] contain the same values, using bag semantics. */
fun Relation.same(other: Relation): Boolean {
  val a = iterator().asSequence().toSet()
  val b = other.iterator().asSequence().toSet()
  return a == b
}

// TODO : Document this.
sealed interface Column {
  data class Constant(val value: Atom) : Column
  data class Index(val index: Int) : Column
}

// TODO : Document this.
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
              .asAtomList())
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
): Relation {
  val sequence = sequence { builder() }
  return object : Relation() {
    override val arity = arity
    override fun iterator() = sequence.iterator()
  }
}
