package io.github.alexandrepiveteau.datalog.core.interpreter

import io.github.alexandrepiveteau.datalog.core.NoStratificationException
import io.github.alexandrepiveteau.datalog.core.interpreter.database.FactsDatabase
import io.github.alexandrepiveteau.datalog.core.interpreter.database.PredicateWithArity
import io.github.alexandrepiveteau.datalog.core.interpreter.database.RulesDatabase
import io.github.alexandrepiveteau.datalog.core.rule.AggregationRule
import io.github.alexandrepiveteau.datalog.core.rule.CombinationRule
import io.github.alexandrepiveteau.datalog.core.rule.Rule
import io.github.alexandrepiveteau.graphs.Vertex
import io.github.alexandrepiveteau.graphs.algorithms.stronglyConnectedComponentsKosaraju
import io.github.alexandrepiveteau.graphs.algorithms.topologicalSort
import io.github.alexandrepiveteau.graphs.arcTo
import io.github.alexandrepiveteau.graphs.builder.buildDirectedGraph
import io.github.alexandrepiveteau.graphs.forEach
import io.github.alexandrepiveteau.graphs.toTypedArray

/**
 * Returns the [Set] of all [PredicateWithArity] which the [target] [PredicateWithArity] depends on.
 * These are the predicates which must be evaluated before or simultaneously to the [target]
 * [PredicateWithArity].
 *
 * @param target the target [PredicateWithArity] to evaluate.
 * @return the [Set] of all [PredicateWithArity] which the [target] [PredicateWithArity] depends on.
 */
private fun RulesDatabase<*>.dependencies(target: PredicateWithArity): Set<PredicateWithArity> {
  val visited = mutableSetOf<PredicateWithArity>()
  val queue = ArrayDeque<PredicateWithArity>().apply { add(target) }
  while (queue.isNotEmpty()) {
    val predicate = queue.removeFirst()
    if (!visited.add(predicate)) continue
    for (rule in this[predicate]) {
      for (clause in rule.body) queue.add(PredicateWithArity(clause.predicate, clause.arity))
    }
  }
  return visited
}

/**
 * Returns the [List] of different strata, each of which should be evaluated in parallel.
 *
 * @param predicates the [Set] of all [PredicateWithArity] which should be evaluated.
 * @param database the [RulesDatabase] which contains the rules.
 */
private fun stratify(
    predicates: Set<PredicateWithArity>,
    database: RulesDatabase<*>,
): List<Set<PredicateWithArity>> {
  // 1. Compute the different strata using the rules.
  // 2. Use a topological sort to order the strata for evaluation.
  val rulesToVertices = mutableMapOf<PredicateWithArity, Vertex>()
  val verticesToRules = mutableMapOf<Vertex, PredicateWithArity>()
  val graph = buildDirectedGraph {
    for (id in predicates) {
      val vertex = addVertex()
      rulesToVertices[id] = vertex
      verticesToRules[vertex] = id
    }
    for (id in predicates) {
      for (rule in database[id]) {
        for (clause in rule.body) {
          val fromKey = PredicateWithArity(clause.predicate, clause.arity)
          val toKey = PredicateWithArity(rule.head.predicate, rule.head.arity)
          val from = rulesToVertices[fromKey] ?: error("No vertex for $fromKey")
          val to = rulesToVertices[toKey] ?: error("No vertex for $toKey")
          addArc(from arcTo to)
        }
      }
    }
  }
  val (scc, map) = graph.stronglyConnectedComponentsKosaraju()
  val strata = mutableMapOf<Vertex, MutableSet<PredicateWithArity>>()

  map.forEach { v, component ->
    val stratum = strata.getOrPut(component) { mutableSetOf() }
    stratum.add(verticesToRules[v] ?: error("No rule for $v"))
  }

  return scc.topologicalSort().toTypedArray().map { vertex ->
    strata[vertex] ?: error("No stratum")
  }
}

/**
 * Returns true if the [RulesDatabase] has an invalid dependency, and false otherwise.
 *
 * @param rules the [RulesDatabase] to evaluate.
 * @param invalid the predicate to use to determine if a dependency is invalid.
 */
private inline fun <T> List<Set<PredicateWithArity>>.hasInvalidDependency(
    rules: RulesDatabase<T>,
    invalid: (seen: Set<PredicateWithArity>, rule: Rule<T>) -> Boolean,
): Boolean {
  val seen = mutableSetOf<PredicateWithArity>()
  for (stratum in this) {
    for (predicate in stratum) {
      for (rule in rules[predicate]) {
        if (invalid(seen, rule)) return true
      }
    }
    seen.addAll(stratum)
  }
  return false
}

/** Returns true if the [RulesDatabase] has a cycle, and false otherwise. */
private fun <T> List<Set<PredicateWithArity>>.hasCycle(rules: RulesDatabase<T>): Boolean {
  return hasInvalidDependency(rules) { seen, rule ->
    when (rule) {
      is CombinationRule -> {
        for (clause in rule.body) {
          // Cyclic dependencies are not allowed for negated clauses.
          val other = PredicateWithArity(clause.predicate, clause.arity)
          if (clause.negated && other !in seen) return true
        }
      }
      is AggregationRule -> {
        // Cyclic dependencies are not allowed for aggregation rules.
        val predicate = PredicateWithArity(rule.clause.predicate, rule.clause.arity)
        if (predicate !in seen) return true
      }
    }
    false
  }
}

/**
 * [stratifiedEval] performs stratified evaluation of the rules, and returns the resulting
 * [FactsDatabase].
 *
 * @param target the target [PredicateWithArity] to evaluate.
 * @param idb the [RulesDatabase] to evaluate.
 * @param edb the [FactsDatabase] to evaluate.
 * @param evalStrata the evaluator to use for each stratum.
 * @return the resulting [FactsDatabase].
 */
internal fun <T> stratifiedEval(
    target: PredicateWithArity,
    idb: RulesDatabase<T>,
    edb: FactsDatabase<T>,
    evalStrata: (RulesDatabase<T>, FactsDatabase<T>) -> FactsDatabase<T>,
): FactsDatabase<T> {
  val dependencies = idb.dependencies(target)
  val order = stratify(dependencies, idb)
  if (order.hasCycle(idb)) throw NoStratificationException()

  var result = edb
  for (stratum in order) {
    val rules = idb.filter(stratum)
    result += evalStrata(rules, result)
  }
  return result
}
