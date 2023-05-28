package io.github.alexandrepiveteau.datalog.core.interpreter

import io.github.alexandrepiveteau.datalog.core.*
import io.github.alexandrepiveteau.datalog.core.rule.*

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

  private fun Rule<T>.limited(): Set<Variable> {
    val variables = mutableSetOf<Variable>()
    body
        .asSequence()
        .filter { !it.negated }
        .forEach { clause -> variables.addAll(clause.atoms.filterIsInstance<Variable>()) }
    when (this) {
      is CombinationRule -> Unit
      is AggregationRule -> variables.add(this.aggregate.result)
    }
    return variables
  }

  private fun requireGrounding(rule: Rule<T>) {
    val head = rule.head.atoms.filterIsInstance<Variable>()
    val body = rule.body.flatMap { it.atoms }.filterIsInstance<Variable>()
    val limited = rule.limited()
    for (variable in head + body) {
      if (variable !in limited) throw NotGroundedException()
    }
  }

  override fun rule(predicate: Predicate, atoms: List<Atom<T>>, block: RuleBuilder<T>.() -> Unit) {
    rule(DatalogRuleBuilder<T>().apply(block).toRule(predicate, atoms))
  }

  override fun rule(rule: Rule<T>) {
    requireGrounding(rule)
    rules.add(rule)
  }

  override fun build(): Program<T> = DatalogProgram(domain, rules, algorithm)
}
