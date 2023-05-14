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
      idbBuilder.getOrPut(predicate) { mutableSetOf() }
    } else {
      idbBuilder.getOrPut(predicate) { mutableSetOf() } += rule
      // TODO : We may not need to store the clauses here if we build the stratification graph
      //        by starting from the final predicate we want to derive, and computing the set of
      //        predicates that are required to derive it iteratively.
      rule.clauses.forEach {
        val clausePredicate = PredicateWithArity(it.predicate, it.arity)
        idbBuilder.getOrPut(clausePredicate) { mutableSetOf() }
      }
    }
  }

  return MapRulesDatabase(idbBuilder) to edbBuilder.build()
}
