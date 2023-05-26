package io.github.alexandrepiveteau.datalog.core.interpreter

import io.github.alexandrepiveteau.datalog.core.Algorithm
import io.github.alexandrepiveteau.datalog.core.Program
import io.github.alexandrepiveteau.datalog.core.ProgramBuilder
import io.github.alexandrepiveteau.datalog.core.RuleBuilder
import io.github.alexandrepiveteau.datalog.core.rule.Atom
import io.github.alexandrepiveteau.datalog.core.rule.Predicate
import io.github.alexandrepiveteau.datalog.core.rule.Rule
import io.github.alexandrepiveteau.datalog.core.rule.Variable
import io.github.alexandrepiveteau.datalog.dsl.Domain

/**
 * An implementation of [ProgramBuilder] which can be used to build a [Program].
 *
 * @param T the type of the constants in the program.
 * @param domain the [Domain] of the constants in the program.
 *     @param algorithm the [Algorithm] used to evaluate each stratum.
 */
internal class DatalogProgramBuilder<T>(
    private val domain: Domain<T>,
    private val algorithm: Algorithm,
) : ProgramBuilder<T> {

  private var nextRelation = 0
  private var nextVariable = 0

  override fun predicate(): Predicate = Predicate(nextRelation++)
  override fun variable(): Variable = Variable(nextVariable++)

  private val rules = mutableSetOf<Rule<T>>()

  override fun rule(predicate: Predicate, atoms: List<Atom<T>>, block: RuleBuilder<T>.() -> Unit) {
    rules.add(DatalogRuleBuilder<T>().apply(block).toRule(predicate, atoms))
  }

  override fun build(): Program<T> = DatalogProgram(domain, rules, algorithm)
}
