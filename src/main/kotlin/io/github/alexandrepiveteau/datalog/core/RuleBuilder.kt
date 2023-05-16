package io.github.alexandrepiveteau.datalog.core

/**
 * A [RuleBuilder] is a mutable builder for the body of a [Program] rule. It is used to define the
 * dependencies of a [Predicate] on other [Predicate]s.
 */
interface RuleBuilder {

  /** An enumeration representing the different kinds of aggregates available. */
  enum class Aggregate {

    /** Returns the minimum value. */
    Min,

    /** Returns the maximum value. */
    Max,
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
   * The values present in [column] in the other [predicate] will be aggregated, and the result will
   * be stored in the variable [result], which may be used in the head of the rule.
   *
   * @param operator the [Aggregate] operator to use.
   * @param same the [AtomList] of variables that are present in the other [predicate], and for
   *   which the values will be aggregated if they are equal.
   * @param column the [Atom] of the other [predicate] whose values will be aggregated.
   * @param result the [Atom] in which the result of the aggregation will be stored.
   */
  fun aggregate(operator: Aggregate, same: AtomList, column: Atom, result: Atom)
}
