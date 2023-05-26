package io.github.alexandrepiveteau.datalog.core.interpreter

import io.github.alexandrepiveteau.datalog.core.interpreter.database.*
import io.github.alexandrepiveteau.datalog.core.rule.*

private fun <T> requireFact(rule: CombinationRule<T>): Fact<T> {
  require(rule.body.isEmpty()) { "Rule is not a fact." }
  val result = rule.head.atoms.filterIsInstance<Value<T>>()
  require(result.size == rule.head.arity) { "This fact has some unsafe variables." }
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
        val predicate = PredicateWithArity(rule.head.predicate, rule.head.arity)
        if (rule.body.isEmpty()) {
          edbBuilder.add(predicate, requireFact(rule))
        } else {
          idbBuilder.getOrPut(predicate) { mutableSetOf() } += rule
        }
      }
      is AggregationRule -> {
        val predicate = PredicateWithArity(rule.head.predicate, rule.head.arity)
        idbBuilder.getOrPut(predicate) { mutableSetOf() } += rule
      }
    }
  }

  return MapRulesDatabase(idbBuilder) to edbBuilder.build()
}
