package io.github.alexandrepiveteau.datalog.core

import io.github.alexandrepiveteau.datalog.core.interpreter.Context
import io.github.alexandrepiveteau.datalog.core.interpreter.database.FactsDatabase
import io.github.alexandrepiveteau.datalog.core.interpreter.database.RulesDatabase
import io.github.alexandrepiveteau.datalog.core.interpreter.ir.Database
import io.github.alexandrepiveteau.datalog.core.interpreter.ir.IROp
import io.github.alexandrepiveteau.datalog.core.interpreter.naiveEval
import io.github.alexandrepiveteau.datalog.core.interpreter.semiNaiveEval

/** The different modes of evaluation which can be used to solve a program. */
enum class Algorithm {

  /** Naive evaluation. */
  Naive {
    override fun <T> evaluate(
        context: Context<T>,
        rules: RulesDatabase<T>,
        base: Database,
        result: Database,
    ) = with(context) { naiveEval(rules, base, result) }
  },

  /** Semi-naive evaluation. */
  SemiNaive {
    override fun <T> evaluate(
        context: Context<T>,
        rules: RulesDatabase<T>,
        base: Database,
        result: Database,
    ) = with(context) { semiNaiveEval(rules, base, result) }
  };

  /**
   * Evaluates the program using the specified [context], [rules] and [facts], and returns the
   * resulting [FactsDatabase]. The [rules] should all be evaluated simultaneously, and the [facts]
   * should be the base facts.
   *
   * Stratified programs will call the [evaluate] method once for each stratum, in order. The
   * [facts] will be updated with the new facts, and the [rules] will be updated with the new rules.
   *
   * @param context the [Context] used to evaluate the program.
   * @param rules the [RulesDatabase] used to evaluate the program.
   * @param facts the [FactsDatabase] used to evaluate the program.
   */
  internal abstract fun <T> evaluate(
      context: Context<T>,
      rules: RulesDatabase<T>,
      base: Database,
      result: Database,
  ): IROp<T>
}
