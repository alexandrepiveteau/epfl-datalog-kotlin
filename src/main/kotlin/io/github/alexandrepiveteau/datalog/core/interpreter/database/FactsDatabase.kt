package io.github.alexandrepiveteau.datalog.core.interpreter.database

import io.github.alexandrepiveteau.datalog.core.interpreter.algebra.Relation

/**
 * A database of facts which have been derived.
 *
 * @param T the type of the elements in the relations.
 */
internal interface FactsDatabase<T> : Iterable<PredicateWithArity> {

  /** Returns an [Iterator] over all the [PredicateWithArity] in this [FactsDatabase]. */
  override operator fun iterator(): Iterator<PredicateWithArity>

  /**
   * Returns the [Relation] associated to the given [PredicateWithArity], or an empty [Relation] if
   * none is found.
   */
  operator fun get(predicate: PredicateWithArity): Relation<T>

  /**
   * Returns a new [FactsDatabase] with the contents of this [FactsDatabase] and the other
   * [FactsDatabase]. Relations present in both [FactsDatabase]s will be merged.
   *
   * @param other the other [FactsDatabase].
   */
  operator fun plus(other: FactsDatabase<T>): FactsDatabase<T>

  /** Returns a new [MutableFactsDatabase] with the same contents as this [FactsDatabase]. */
  fun toMutableFactDatabase(): MutableFactsDatabase<T>
}

/**
 * A mutable [FactsDatabase].
 *
 * @param T the type of the elements in the relations.
 */
internal interface MutableFactsDatabase<T> : FactsDatabase<T> {

  /**
   * Adds the values present in [other] to this [MutableFactsDatabase]. Relations present in both
   * [MutableFactsDatabase]s will be merged.
   */
  operator fun plusAssign(other: FactsDatabase<T>)

  /**
   * Replaces the [Relation] associated to the given [PredicateWithArity] with the given [Relation].
   *
   * @param predicate the [PredicateWithArity] to replace.
   * @param relation the [Relation] to replace with.
   */
  operator fun set(predicate: PredicateWithArity, relation: Relation<T>)

  companion object
}

/**
 * Returns `false` if this [FactsDatabase] contains at least one fact for the given
 * [PredicateWithArity].
 */
internal fun <T> FactsDatabase<T>.isEmpty(): Boolean = all { this[it].tuples.isEmpty() }

/**
 * Returns `true` if this [FactsDatabase] contains at least one fact for the given
 * [PredicateWithArity].
 */
internal fun <T> FactsDatabase<T>.isNotEmpty(): Boolean = !isEmpty()
