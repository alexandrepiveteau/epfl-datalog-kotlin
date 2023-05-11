package io.github.alexandrepiveteau.datalog.core

import io.github.alexandrepiveteau.datalog.core.interpreter.DatalogProgram

/**
 * A [ProgramBuilder] is a mutable builder for [Program] instances. It is used to build a [Program]
 * from a set of rules, which can then be used to compute results.
 */
interface ProgramBuilder {

  // TODO : Provide some faster APIs to create contiguous lists of variables or constants (useful
  //        for defining a domain in a single operation).

  /** Adds a new [Relation] to the program, which may be used in one or more [rule]s. */
  fun relation(): Relation

  /** Adds a new variable to the program. */
  fun variable(): Atom

  /** Adds a new constant to the program. */
  fun constant(): Atom

  // TODO : Provide some allocation-free ways to build lists of atoms or of rules, with lower-level
  //        APIs which work with a stack-based API hidden behind internal rules.

  /**
   * Adds a new fact to the program, which is a rule without a body. The [atoms] must be constants.
   *
   * @param relation the [Relation] to which the fact belongs.
   * @param atoms the [AtomList] of constants that make up the fact.
   */
  fun fact(relation: Relation, atoms: AtomList) = rule(relation, atoms) {}

  /**
   * Adds a new rule to the program, which is a [Relation] with a body. The [atoms] must be
   * constants or variables.
   *
   * @param relation the [Relation] to which the rule belongs.
   * @param atoms the [AtomList] of constants or variables that make up the rule.
   * @param block the [RuleBuilder] which will be used to build the body of the rule.
   */
  fun rule(relation: Relation, atoms: AtomList, block: RuleBuilder.() -> Unit)

  /** Returns a [Program] built from the current builder, and on which results can be computed. */
  fun build(): Program
}

/**
 * Returns a [ProgramBuilder], which can be used to build a [Program] and obtain results from it.
 */
fun ProgramBuilder(): ProgramBuilder = DatalogProgram()
