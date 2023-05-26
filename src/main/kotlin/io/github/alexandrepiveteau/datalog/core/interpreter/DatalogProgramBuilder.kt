package io.github.alexandrepiveteau.datalog.core.interpreter

import io.github.alexandrepiveteau.datalog.core.Program
import io.github.alexandrepiveteau.datalog.core.ProgramBuilder
import io.github.alexandrepiveteau.datalog.core.RuleBuilder
import io.github.alexandrepiveteau.datalog.core.interpreter.database.FactsDatabase
import io.github.alexandrepiveteau.datalog.core.interpreter.database.RulesDatabase
import io.github.alexandrepiveteau.datalog.core.rule.Atom
import io.github.alexandrepiveteau.datalog.core.rule.Predicate
import io.github.alexandrepiveteau.datalog.core.rule.Rule
import io.github.alexandrepiveteau.datalog.core.rule.Variable
import io.github.alexandrepiveteau.datalog.dsl.Domain

/**
 * An implementation of [ProgramBuilder] which can be used to build a [Program].
 *
 * @param evalStrata the function used to evaluate each stratum.
 */
internal class DatalogProgramBuilder<T>
private constructor(
    private val domain: Domain<T>,
    private val evalStrata:
        (Context<T>) -> (RulesDatabase<T>, FactsDatabase<T>) -> FactsDatabase<T>,
) : ProgramBuilder<T> {

  private var nextRelation = 0
  private var nextVariable = 0

  override fun predicate(): Predicate = Predicate(nextRelation++)
  override fun variable(): Variable<T> = Variable(nextVariable++)

  private val rules = mutableSetOf<Rule<T>>()

  override fun rule(predicate: Predicate, atoms: List<Atom<T>>, block: RuleBuilder<T>.() -> Unit) {
    rules.add(DatalogRuleBuilder<T>().apply(block).toRule(predicate, atoms))
  }

  override fun build(): Program<T> = DatalogProgram(domain, rules, evalStrata)

  companion object {

    /** Returns a [DatalogProgramBuilder] which executes using naive evaluation. */
    fun <T> naive(
        domain: Domain<T>,
    ): DatalogProgramBuilder<T> = DatalogProgramBuilder(domain) { with(it) { ::naiveEval } }

    /** Returns a [DatalogProgramBuilder] which executes using semi-naive evaluation. */
    fun <T> semiNaive(
        domain: Domain<T>,
    ): DatalogProgramBuilder<T> = DatalogProgramBuilder(domain) { with(it) { ::semiNaiveEval } }
  }
}
