package io.github.alexandrepiveteau.datalog.core

import io.github.alexandrepiveteau.datalog.core.interpreter.DatalogProgramBuilder

/**
 * A [ProgramBuilder] is a mutable builder for [Program] instances. It is used to build a [Program]
 * from a set of rules, which can then be used to compute results.
 */
interface ProgramBuilder {

  // TODO : Provide some faster APIs to create contiguous lists of variables or constants (useful
  //        for defining a domain in a single operation).

  /** Adds a new [Predicate] to the program, which may be used in one or more [rule]s. */
  fun predicate(): Predicate

  /** Adds a new variable to the program. */
  fun variable(): Atom

  /** Adds a new constant to the program. */
  fun constant(): Atom

  // TODO : Provide some allocation-free ways to build lists of atoms or of rules, with lower-level
  //        APIs which work with a stack-based API hidden behind internal rules.

  /**
   * Adds a new fact to the program, which is a rule without a body. The [atoms] must be constants.
   *
   * @param predicate the [Predicate] to which the fact belongs.
   * @param atoms the [AtomList] of constants that make up the fact.
   */
  fun fact(predicate: Predicate, atoms: AtomList) = rule(predicate, atoms) {}

  /**
   * Adds a new rule to the program, which is a [Predicate] with a body. The [atoms] must be
   * constants or variables.
   *
   * @param predicate the [Predicate] to which the rule belongs.
   * @param atoms the [AtomList] of constants or variables that make up the rule.
   * @param block the [RuleBuilder] which will be used to build the body of the rule.
   */
  fun rule(predicate: Predicate, atoms: AtomList, block: RuleBuilder.() -> Unit)

  /**
   * Returns a [Program] built from the current builder, and on which results can be computed.
   *
   * @param domain the [Domain] on which the results are computed.
   */
  fun build(domain: Domain): Program
}

/**
 * Returns a [ProgramBuilder], which can be used to build a [Program] and obtain results from it.
 *
 * @param algorithm the [Algorithm] that should be used to compute the results.
 */
fun ProgramBuilder(algorithm: Algorithm = Algorithm.Naive): ProgramBuilder {
  return when (algorithm) {
    Algorithm.Naive -> DatalogProgramBuilder.naive()
    Algorithm.SemiNaive -> DatalogProgramBuilder.semiNaive()
  }
}
