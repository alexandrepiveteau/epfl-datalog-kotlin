package io.github.alexandrepiveteau.datalog.core.interpreter

import io.github.alexandrepiveteau.datalog.core.interpreter.database.*
import io.github.alexandrepiveteau.datalog.dsl.Value

private fun <T> requireFact(rule: CombinationRule<T>): List<Value<T>> {
  require(rule.clauses.isEmpty()) { "Rule is not a fact." }
  val result = rule.atoms.filterIsInstance<Value<T>>()
  require(result.size == rule.arity) { "This fact has some unsafe variables." }
  return result
}

/**
 * Partitions the given collection of rules in an [RulesDatabase] and an [FactsDatabase] pair, based
 * on the presence of clauses in the rules.
 *
 * @param rules the [Collection] of [Rule]s to partition.
 * @return a pair of [RulesDatabase] and [FactsDatabase] instances.
 */
internal fun <T> partition(rules: Collection<Rule<T>>): Pair<RulesDatabase<T>, FactsDatabase<T>> {
  val edbBuilder = MutableFactsDatabase.builder<T>()
  val idbBuilder = mutableMapOf<PredicateWithArity, MutableSet<Rule<T>>>()

  for (rule in rules) {
    when (rule) {
      is CombinationRule -> {
        val predicate = PredicateWithArity(rule.predicate, rule.arity)
        if (rule.clauses.isEmpty()) {
          edbBuilder.add(predicate, requireFact(rule))
        } else {
          idbBuilder.getOrPut(predicate) { mutableSetOf() } += rule
        }
      }
      is AggregationRule -> {
        val predicate = PredicateWithArity(rule.predicate, rule.arity)
        idbBuilder.getOrPut(predicate) { mutableSetOf() } += rule
      }
    }
  }

  return MapRulesDatabase(idbBuilder) to edbBuilder.build()
}
