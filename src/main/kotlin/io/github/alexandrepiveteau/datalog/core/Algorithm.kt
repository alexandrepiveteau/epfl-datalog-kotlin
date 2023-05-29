package io.github.alexandrepiveteau.datalog.core

import io.github.alexandrepiveteau.datalog.core.interpreter.Context
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
   * Evaluates the program using the specified [context], [rules] and [base] facts, and returns the
   * resulting [result] database. The [rules] will all be evaluated simultaneously until a fixpoint
   * is reached.
   *
   * Stratified programs will call the [evaluate] method once for each stratum, in order. The facts
   * will be updated with the new facts, and the [rules] will be updated with the new rules.
   *
   * @param context the [Context] used to evaluate the program.
   * @param rules the [RulesDatabase] used to evaluate the program.
   * @param base the [Database] containing the base facts.
   * @param result the [Database] in which results should be stored.
   */
  internal abstract fun <T> evaluate(
      context: Context<T>,
      rules: RulesDatabase<T>,
      base: Database,
      result: Database,
  ): IROp<T>
}
