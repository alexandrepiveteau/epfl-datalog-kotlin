package io.github.alexandrepiveteau.datalog.core

import io.github.alexandrepiveteau.datalog.core.interpreter.algebra.Column

/**
 * A [RuleBuilder] is a mutable builder for the body of a [Program] rule. It is used to define the
 * dependencies of a [Predicate] on other [Predicate]s.
 */
interface RuleBuilder {

  /** An enumeration representing the different kinds of aggregates available. */
  enum class Aggregate(
      private val t: Domain.(Set<Int>, AtomList) -> Atom,
      private val c: Domain.(Atom, Atom) -> Atom
  ) {

    /** Returns the number of elements. */
    Count(
        t = { _, _ -> unit() },
        c = Domain::sum,
    ),

    /** Returns the sum. */
    Sum(
        t = { indices, atoms ->
          atoms.toList().filterIndexed { index, _ -> index in indices }.reduce(::sum)
        },
        c = Domain::sum,
    ),

    /** Returns the minimum value. */
    Min(
        t = { indices, atoms ->
          atoms.toList().filterIndexed { index, _ -> index in indices }.reduce(::min)
        },
        c = Domain::min,
    ),

    /** Returns the maximum value. */
    Max(
        t = { indices, atoms ->
          atoms.toList().filterIndexed { index, _ -> index in indices }.reduce(::max)
        },
        c = Domain::max,
    );

    /** Returns the result of the [transform] operation on the [AtomList] of [Column.Index]es. */
    internal fun Domain.transform(
        indices: Set<Column.Index>,
        atoms: AtomList,
    ): Atom = t(indices.mapTo(mutableSetOf()) { it.index }, atoms)

    /** Returns the result of the [combine] operation on the two [Atom]s. */
    internal fun Domain.combine(
        first: Atom,
        second: Atom,
    ): Atom = c(first, second)
  }

  /**
   * Adds a new [Predicate] to the body of the rule, with the given [atoms] and [negated] flag, if
   * the rule is negated in the body.
   *
   * @param predicate the [Predicate] to which the rule belongs.
   * @param atoms the [AtomList] of constants or variables that make up the rule.
   * @param negated true if the rule is negated in the body.
   */
  fun predicate(predicate: Predicate, atoms: AtomList, negated: Boolean)

  /**
   * Performs an [aggregate] operation on the values of another [predicate]. In order to perform an
   * [aggregate], a single [predicate] must have been added to the body of the rule. The [same]
   * [AtomList] must be a list of variables present in the other [predicate].
   *
   * The values present in [columns] in the other [predicate] will be aggregated, and the result
   * will be stored in the variable [result], which may be used in the head of the rule.
   *
   * @param operator the [Aggregate] operator to use.
   * @param same the [AtomList] of variables that are present in the other [predicate], and for
   *   which the values will be aggregated if they are equal.
   * @param columns the [AtomList] of the other [predicate] whose values will be aggregated.
   * @param result the [Atom] in which the result of the aggregation will be stored.
   */
  fun aggregate(operator: Aggregate, same: AtomList, columns: AtomList, result: Atom)
}
