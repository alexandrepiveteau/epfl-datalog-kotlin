package io.github.alexandrepiveteau.datalog.core.interpreter

import io.github.alexandrepiveteau.datalog.core.*
import io.github.alexandrepiveteau.datalog.core.interpreter.database.FactsDatabase
import io.github.alexandrepiveteau.datalog.core.interpreter.database.RulesDatabase

/**
 * An implementation of [ProgramBuilder] which can be used to build a [Program].
 *
 * @param evalStrata the function used to evaluate each stratum.
 */
internal class DatalogProgramBuilder
private constructor(
    private val evalStrata: (Context) -> (RulesDatabase, FactsDatabase) -> FactsDatabase,
) : ProgramBuilder {

  private var nextRelation = 0
  private var nextVariable = -1
  private var nextConstant = 0

  override fun predicate(): Predicate = Predicate(nextRelation++)
  override fun variable(): Atom = Atom(nextVariable--)
  override fun constant(): Atom = Atom(nextConstant++)

  private val rules = mutableSetOf<Rule>()

  override fun rule(predicate: Predicate, atoms: AtomList, block: RuleBuilder.() -> Unit) {
    rules.add(DatalogRuleBuilder().apply(block).toRule(predicate, atoms))
  }

  override fun build(
      domain: Domain,
  ): Program = DatalogProgram(domain, rules, nextConstant, evalStrata)

  companion object {

    /** Returns a [DatalogProgramBuilder] which executes using naive evaluation. */
    fun naive(): DatalogProgramBuilder = DatalogProgramBuilder { with(it) { ::naiveEval } }

    /** Returns a [DatalogProgramBuilder] which executes using semi-naive evaluation. */
    fun semiNaive(): DatalogProgramBuilder = DatalogProgramBuilder { with(it) { ::semiNaiveEval } }
  }
}
