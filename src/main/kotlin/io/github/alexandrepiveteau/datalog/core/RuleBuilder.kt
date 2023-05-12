package io.github.alexandrepiveteau.datalog.core

/**
 * A [RuleBuilder] is a mutable builder for the body of a [Program] rule. It is used to define the
 * dependencies of a [Predicate] on other [Predicate]s.
 */
fun interface RuleBuilder {

  /**
   * Adds a new [Predicate] to the body of the rule, with the given [atoms] and [negated] flag, if
   * the rule is negated in the body.
   *
   * @param predicate the [Predicate] to which the rule belongs.
   * @param atoms the [AtomList] of constants or variables that make up the rule.
   * @param negated true if the rule is negated in the body.
   */
  fun body(predicate: Predicate, atoms: AtomList, negated: Boolean)
}
