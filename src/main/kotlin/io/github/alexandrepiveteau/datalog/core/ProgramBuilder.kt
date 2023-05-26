package io.github.alexandrepiveteau.datalog.core

import io.github.alexandrepiveteau.datalog.core.interpreter.DatalogProgramBuilder
import io.github.alexandrepiveteau.datalog.core.rule.Atom
import io.github.alexandrepiveteau.datalog.core.rule.Fact
import io.github.alexandrepiveteau.datalog.core.rule.Predicate
import io.github.alexandrepiveteau.datalog.core.rule.Variable
import io.github.alexandrepiveteau.datalog.dsl.Domain

/**
 * A [ProgramBuilder] is a mutable builder for [Program] instances. It is used to build a [Program]
 * from a set of rules, which can then be used to compute results.
 *
 * @param T the type of the constants in the program.
 */
interface ProgramBuilder<T> {

  /** Adds a new [Predicate] to the program, which may be used in one or more [rule]s. */
  fun predicate(): Predicate

  /** Adds a new variable to the program. */
  fun variable(): Variable<T>

  /**
   * Adds a new fact to the program, which is a rule without a body.
   *
   * @param fact the [Fact] to add to the program.
   */
  fun fact(predicate: Predicate, fact: Fact<T>) = rule(predicate, fact) {}

  /**
   * Adds a new rule to the program, which is a [Predicate] with a body. The [atoms] must be
   * constants or variables.
   *
   * @param predicate the [Predicate] to which the rule belongs.
   * @param atoms the [List] of constants or variables that make up the rule.
   * @param block the [RuleBuilder] which will be used to build the body of the rule.
   */
  fun rule(predicate: Predicate, atoms: List<Atom<T>>, block: RuleBuilder<T>.() -> Unit)

  /** Returns a [Program] built from the current builder, and on which results can be computed. */
  fun build(): Program<T>
}

/**
 * Returns a [ProgramBuilder], which can be used to build a [Program] and obtain results from it.
 *
 * @param algorithm the [Algorithm] that should be used to compute the results.
 * @param T the type of the constants in the program.
 */
fun <T> ProgramBuilder(
    domain: Domain<T>,
    algorithm: Algorithm = Algorithm.Naive,
): ProgramBuilder<T> = DatalogProgramBuilder(domain, algorithm)
