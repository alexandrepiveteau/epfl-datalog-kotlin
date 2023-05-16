package io.github.alexandrepiveteau.datalog.dsl

import io.github.alexandrepiveteau.datalog.core.RuleBuilder

/**
 * The scope in which Datalog programs are handled. The domain of the program is defined as the set
 * of values that appear in the rules or the facts, and automatically inferred from the program.
 *
 * @param T the type of the elements in the relations. Must be comparable.
 */
interface DatalogScope<T : Comparable<T>> {

  /**
   * Returns some [Terms] which are guaranteed to be empty. This is useful when we want to create a
   * new rule with no terms on the right-hand side.
   */
  val empty: Terms<Nothing>
    get() = Terms(emptySet())

  /** Registers the set of constants in the [DatalogScope]. */
  fun constants(vararg values: T)

  /** Returns a new [Variable] which is guaranteed to be unique within this [DatalogScope]. */
  fun variable(): Variable<T>

  /** Returns a [Variables] instance. */
  fun variables(): Variables<T> = Variables { variable() }

  /** Returns a new [Predicate] which is guaranteed to be unique within this [DatalogScope]. */
  fun predicate(): Predicate<T>

  /** Returns a [Predicates] instance. */
  fun predicates(): Predicates<T> = Predicates { predicate() }

  // Utilities for creating terms.
  fun <R> R.asValue(): Value<R> = Value(this)

  // Relation to term operators.
  operator fun Predicate<T>.invoke(vararg atoms: T) = Term(this, atoms.map { Value(it) }, false)
  operator fun Predicate<T>.invoke(vararg atoms: Atom<T>) = Term(this, atoms.toList(), false)
  operator fun Term<T>.not() = copy(negated = !negated)

  // Aggregation functions.

  /** Returns an [Aggregate] to compute the maximum value of a column. */
  fun max(same: Iterable<Variable<T>>, column: Variable<T>, result: Variable<T>): Aggregate<T> =
      Aggregate(RuleBuilder.Aggregate.Max, same.toList(), column, result)

  /** Returns an [Aggregate] to compute the minimum value of a column. */
  fun min(same: Iterable<Variable<T>>, column: Variable<T>, result: Variable<T>): Aggregate<T> =
      Aggregate(RuleBuilder.Aggregate.Min, same.toList(), column, result)

  // Terms operators.
  operator fun Term<T>.plus(term: Term<T>): Terms<T> = Terms(setOf(this, term))
  operator fun Term<T>.plus(terms: Terms<T>): Terms<T> = Terms(setOf(this) + terms.terms)
  operator fun Terms<T>.plus(term: Term<T>): Terms<T> = Terms(this.terms + term)
  operator fun Terms<T>.plus(terms: Terms<T>): Terms<T> = Terms(this.terms + terms.terms)
  operator fun Term<T>.plus(aggregate: Aggregate<T>): Aggregation<T> = Aggregation(this, aggregate)

  // Rules operators.
  operator fun Term<T>.plusAssign(terms: Terms<T>)
  operator fun Term<T>.plusAssign(aggregation: Aggregation<T>)
  operator fun Term<T>.plusAssign(term: Term<T>) = plusAssign(term + empty)

  /**
   * Derives a new [Set] of [Term] that are implied by the given [predicate] and the current set of
   * rules. If the semantics of the program is not well-defined, this function may return an empty
   * set.
   *
   * @param predicate the [Predicate] to solve.
   * @param arity the arity of the [Predicate] to solve.
   */
  fun solve(predicate: Predicate<T>, arity: Int): Set<Term<T>>
}
