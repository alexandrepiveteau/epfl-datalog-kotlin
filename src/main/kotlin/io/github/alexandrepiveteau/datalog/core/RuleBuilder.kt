package io.github.alexandrepiveteau.datalog.core

/**
 * A [RuleBuilder] is a mutable builder for the body of a [Program] rule. It is used to define the
 * dependencies of a [Relation] on other [Relation]s.
 */
fun interface RuleBuilder {

  /**
   * Adds a new [Relation] to the body of the rule, with the given [atoms] and [negated] flag, if
   * the rule is negated in the body.
   *
   * @param relation the [Relation] to which the rule belongs.
   * @param atoms the [AtomList] of constants or variables that make up the rule.
   * @param negated true if the rule is negated in the body.
   */
  fun body(relation: Relation, atoms: AtomList, negated: Boolean)
}
