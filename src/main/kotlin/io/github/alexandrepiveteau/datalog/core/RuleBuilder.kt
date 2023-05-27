package io.github.alexandrepiveteau.datalog.core

import io.github.alexandrepiveteau.datalog.core.interpreter.algebra.Column
import io.github.alexandrepiveteau.datalog.core.rule.*

/**
 * A [RuleBuilder] is a mutable builder for the body of a [Program] rule. It is used to define the
 * dependencies of a [Predicate] on other [Predicate]s.
 */
interface RuleBuilder<in T> {

  /** An enumeration representing the different kinds of aggregates available. */
  enum class AggregationFunction {

    /** Returns the number of elements. */
    Count {
      override fun <T> Domain<T>.transform(
          indices: Set<Column.Index>,
          atoms: Fact<T>,
      ) = unit()
      override fun <T> Domain<T>.combine(
          first: Value<T>,
          second: Value<T>,
      ): Value<T> = sum(first, second)
    },

    /** Returns the sum. */
    Sum {
      override fun <T> Domain<T>.transform(
          indices: Set<Column.Index>,
          atoms: Fact<T>,
      ) = atoms.filterIndexed { index, _ -> Column.Index(index) in indices }.reduce(::sum)
      override fun <T> Domain<T>.combine(
          first: Value<T>,
          second: Value<T>,
      ) = sum(first, second)
    },

    /** Returns the minimum value. */
    Min {

      override fun <T> Domain<T>.transform(
          indices: Set<Column.Index>,
          atoms: Fact<T>,
      ) = atoms.filterIndexed { index, _ -> Column.Index(index) in indices }.reduce(::min)
      override fun <T> Domain<T>.combine(
          first: Value<T>,
          second: Value<T>,
      ) = min(first, second)
    },

    /** Returns the maximum value. */
    Max {
      override fun <T> Domain<T>.transform(
          indices: Set<Column.Index>,
          atoms: Fact<T>,
      ) = atoms.filterIndexed { index, _ -> Column.Index(index) in indices }.reduce(::max)
      override fun <T> Domain<T>.combine(
          first: Value<T>,
          second: Value<T>,
      ) = max(first, second)
    };

    /**
     * Returns the result of the [transform] operation on the [List] of atoms of [Column.Index]es.
     */
    internal abstract fun <T> Domain<T>.transform(
        indices: Set<Column.Index>,
        atoms: Fact<T>,
    ): Value<T>

    /** Returns the result of the [combine] operation on the two [Atom]s. */
    internal abstract fun <T> Domain<T>.combine(
        first: Value<T>,
        second: Value<T>,
    ): Value<T>
  }

  /**
   * Adds a new [Predicate] to the body of the rule, with the given [atoms] and [negated] flag, if
   * the rule is negated in the body.
   *
   * @param predicate the [Predicate] to which the rule belongs.
   * @param atoms the [List] of constants or variables that make up the rule.
   * @param negated true if the rule is negated in the body.
   */
  fun predicate(predicate: Predicate, atoms: List<Atom<T>>, negated: Boolean)

  /**
   * Performs an [aggregate] operation on the values of another [predicate]. In order to perform an
   * [aggregate], a single [predicate] must have been added to the body of the rule. The [same]
   * [Collection] of variables must be a list of variables present in the other [predicate].
   *
   * The values present in [columns] in the other [predicate] will be aggregated, and the result
   * will be stored in the variable [result], which may be used in the head of the rule.
   *
   * @param operator the [AggregationFunction] operator to use.
   * @param same the [List] of variables that are present in the other [predicate], and for which
   *   the values will be aggregated if they are equal.
   * @param columns the [List] of variables the [predicate] whose values will be aggregated.
   * @param result the [Atom] in which the result of the aggregation will be stored.
   */
  fun aggregate(
      operator: AggregationFunction,
      same: Collection<Variable>,
      columns: Collection<Variable>,
      result: Variable,
  )
}
