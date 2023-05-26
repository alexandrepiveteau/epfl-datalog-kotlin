package io.github.alexandrepiveteau.datalog.dsl

import io.github.alexandrepiveteau.datalog.core.Predicate
import io.github.alexandrepiveteau.datalog.core.RuleBuilder

/**
 * The scope in which Datalog programs are handled. The domain of the program is defined as the set
 * of values that appear in the rules or the facts, and automatically inferred from the program.
 *
 * @param T the type of the elements in the relations. Must be comparable.
 */
interface DatalogScope<T> {

  /**
   * Returns some [Terms] which are guaranteed to be empty. This is useful when we want to create a
   * new rule with no terms on the right-hand side.
   */
  val empty: Terms<Nothing>
    get() = Terms(emptySet())

  /** Returns a new [Variable] which is guaranteed to be unique within this [DatalogScope]. */
  fun variable(): Variable<T>

  /** Returns a [Variables] instance. */
  fun variables(): Variables<T> = Variables { variable() }

  /** Returns a new [Predicate] which is guaranteed to be unique within this [DatalogScope]. */
  fun predicate(): Predicate

  /** Returns a [Predicates] instance. */
  fun predicates(): Predicates = Predicates { predicate() }

  // Utilities for creating terms.
  fun <R> R.asValue(): Value<R> = Value(this)

  // Relation to term operators.
  operator fun Predicate.invoke(vararg atoms: T) = Term(this, atoms.map { Value(it) }, false)
  operator fun Predicate.invoke(vararg atoms: Atom<T>) = Term(this, atoms.toList(), false)
  operator fun Term<T>.not() = copy(negated = !negated)

  // Aggregation functions.

  /** Returns an [Aggregate] to compute the number of rows in a relation. */
  fun count(
      same: Collection<Variable<T>>,
      result: Variable<T>,
  ): Aggregate<T> = Aggregate(RuleBuilder.Aggregate.Count, same, emptySet(), result)

  /** Returns an [Aggregate] to compute the total value of some columns. */
  fun sum(
      same: Collection<Variable<T>>,
      columns: Collection<Variable<T>>,
      result: Variable<T>,
  ): Aggregate<T> = Aggregate(RuleBuilder.Aggregate.Sum, same, columns, result)

  /** Returns an [Aggregate] to compute the maximum value of some columns. */
  fun max(
      same: Collection<Variable<T>>,
      columns: Collection<Variable<T>>,
      result: Variable<T>,
  ): Aggregate<T> = Aggregate(RuleBuilder.Aggregate.Max, same, columns, result)

  /** Returns an [Aggregate] to compute the minimum value of some columns. */
  fun min(
      same: Collection<Variable<T>>,
      columns: Collection<Variable<T>>,
      result: Variable<T>,
  ): Aggregate<T> = Aggregate(RuleBuilder.Aggregate.Min, same, columns, result)

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
  fun solve(predicate: Predicate, arity: Int): Set<Term<T>>
}
