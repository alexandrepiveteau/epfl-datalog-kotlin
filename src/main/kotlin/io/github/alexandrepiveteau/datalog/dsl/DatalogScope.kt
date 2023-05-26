package io.github.alexandrepiveteau.datalog.dsl

import io.github.alexandrepiveteau.datalog.core.RuleBuilder
import io.github.alexandrepiveteau.datalog.core.rule.*

/**
 * The scope in which Datalog programs are handled. The domain of the program is defined as the set
 * of values that appear in the rules or the facts, and automatically inferred from the program.
 *
 * @param T the type of the elements in the relations. Must be comparable.
 */
interface DatalogScope<T> {

  /**
   * Returns some [BodyLiterals] which are guaranteed to be empty. This is useful when we want to
   * create a new rule with no terms on the right-hand side.
   */
  val empty: BodyLiterals<Nothing>
    get() = BodyLiterals(emptySet())

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
  operator fun Predicate.invoke(vararg atoms: T) = BodyLiteral(this, atoms.map { Value(it) }, false)
  operator fun Predicate.invoke(vararg atoms: Atom<T>) = BodyLiteral(this, atoms.toList(), false)
  operator fun BodyLiteral<T>.not() = copy(negated = !negated)

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
  operator fun BodyLiteral<T>.plus(
      term: BodyLiteral<T>,
  ): BodyLiterals<T> = BodyLiterals(setOf(this, term))

  operator fun BodyLiteral<T>.plus(
      terms: BodyLiterals<T>,
  ): BodyLiterals<T> = BodyLiterals(setOf(this) + terms.terms)

  operator fun BodyLiterals<T>.plus(
      term: BodyLiteral<T>,
  ): BodyLiterals<T> = BodyLiterals(this.terms + term)

  operator fun BodyLiterals<T>.plus(
      terms: BodyLiterals<T>,
  ): BodyLiterals<T> = BodyLiterals(this.terms + terms.terms)

  operator fun BodyLiteral<T>.plus(
      aggregate: Aggregate<T>,
  ): Aggregation<T> = Aggregation(this, aggregate)

  // Rules operators.
  operator fun BodyLiteral<T>.plusAssign(terms: BodyLiterals<T>)
  operator fun BodyLiteral<T>.plusAssign(aggregation: Aggregation<T>)
  operator fun BodyLiteral<T>.plusAssign(term: BodyLiteral<T>) = plusAssign(term + empty)

  /**
   * Derives a new [Set] of [Term] that are implied by the given [predicate] and the current set of
   * rules. If the semantics of the program is not well-defined, this function may return an empty
   * set.
   *
   * @param predicate the [Predicate] to solve.
   * @param arity the arity of the [Predicate] to solve.
   */
  fun solve(predicate: Predicate, arity: Int): Set<Fact<T>>
}
