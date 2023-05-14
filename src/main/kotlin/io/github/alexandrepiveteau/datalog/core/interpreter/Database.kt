package io.github.alexandrepiveteau.datalog.core.interpreter

import io.github.alexandrepiveteau.datalog.core.interpreter.database.*

/**
 * Partitions the given collection of rules in an [RulesDatabase] and an [FactsDatabase] pair, based
 * on the presence of clauses in the rules.
 *
 * @param rules the [Collection] of [PredicateRule]s to partition.
 * @return a pair of [RulesDatabase] and [FactsDatabase] instances.
 */
internal fun partition(rules: Collection<PredicateRule>): Pair<RulesDatabase, FactsDatabase> {
  val edbBuilder = MutableFactsDatabase.builder()
  val idbBuilder = mutableMapOf<PredicateWithArity, MutableSet<PredicateRule>>()

  for (rule in rules) {
    val predicate = PredicateWithArity(rule.predicate, rule.arity)
    if (rule.clauses.isEmpty()) {
      edbBuilder.add(predicate, rule.atoms)
    } else {
      idbBuilder.getOrPut(predicate) { mutableSetOf() } += rule
    }
  }

  return MapRulesDatabase(idbBuilder) to edbBuilder.build()
}
