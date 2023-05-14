package io.github.alexandrepiveteau.datalog.core.interpreter

import io.github.alexandrepiveteau.datalog.core.*
import io.github.alexandrepiveteau.datalog.core.interpreter.algebra.*
import io.github.alexandrepiveteau.datalog.core.interpreter.algebra.Column.Constant
import io.github.alexandrepiveteau.datalog.core.interpreter.algebra.Column.Index
import io.github.alexandrepiveteau.datalog.core.interpreter.database.*
import io.github.alexandrepiveteau.graphs.Vertex
import io.github.alexandrepiveteau.graphs.algorithms.stronglyConnectedComponentsKosaraju
import io.github.alexandrepiveteau.graphs.algorithms.topologicalSort
import io.github.alexandrepiveteau.graphs.arcTo
import io.github.alexandrepiveteau.graphs.builder.buildDirectedGraph
import io.github.alexandrepiveteau.graphs.forEach
import io.github.alexandrepiveteau.graphs.toTypedArray

/**
 * Returns the [Set] of all the indices of the variables in the [PredicateRule]. This is used to
 * generate the selection clauses.
 */
private fun variableIndices(rule: AtomList): Set<List<Int>> {
  val variables = mutableMapOf<Atom, MutableList<Int>>()
  rule.forEachIndexed { index, atom ->
    if (atom.isVariable) variables.getOrPut(atom) { mutableListOf() }.add(index)
  }
  return variables.values.map { it.toList() }.toSet()
}

/**
 * Returns the [List] of indices for constants in the [PredicateRule]. This is used to generate the
 * clauses that filter the constants.
 */
private fun constantIndices(rule: AtomList): List<Int> {
  val constants = mutableListOf<Int>()
  rule.forEachIndexed { index, atom -> if (atom.isConstant) constants.add(index) }
  return constants
}

/**
 * Returns the [List] of [Column]s that should be used for the projection. This is used to generate
 * the projection clauses.
 */
private fun projection(predicate: AtomList, rule: AtomList): List<Column> {
  return predicate.map { atom ->
    if (atom.isVariable) {
      val index = rule.toList().indexOfFirst { atom == it }
      Index(index)
    } else {
      Constant(atom)
    }
  }
}

/** Returns the [Set] of [Set] of [Column] that should be used for equality selection. */
private fun selection(rule: AtomList): Set<Set<Column>> {
  return buildSet {
    val constants = constantIndices(rule)
    for (variable in variableIndices(rule)) {
      add(variable.mapTo(mutableSetOf()) { Index(it) })
    }
    for (constant in constants) {
      add(setOf(Constant(rule[constant]), Index(constant)))
    }
  }
}

/**
 * [evalRule] takes a [Rule] and a list of [Relation]s, and evaluates it against the list of
 * [Relation]s, returning the values that could be derived as a new [Relation].
 *
 * The rule must have exactly one clause for each evaluated [Relation].
 *
 * @param rule the [Rule] to evaluate.
 * @param relations the [Relation]s to evaluate the rule against.
 */
private fun Context.evalRule(rule: Rule, relations: List<Relation>): Relation {
  require(rule.clauses.size == relations.size) { "Not the same number of relations and clauses." }
  // 1. Negate all the relations that are negated in the rule.
  // 2. Generate a concatenation of all atoms in the rule, after the join.
  // 3. Join all the relations.
  // 4. Select the rows that match the constants and variables.
  // 5. Project the rows to the correct indices, and add constants to the projection.
  val negated = relations.mapIndexed { idx, r -> if (rule.clauses[idx].negated) r.negated() else r }
  val concat = rule.clauses.flatMap { it.atoms.toList() }.asAtomList()
  return negated.join().select(selection(concat)).project(projection(rule.atoms, concat))
}

/**
 * [evalRuleIncremental] takes a [Rule] and two sets of [Relation]s, and evaluates them against the
 * list of [relations] and [incremental] relations, returning the values that could be derived as a
 * new [Relation].
 *
 * For each [incremental] relation, we evaluate the rule with all the [relations] relations and one
 * [incremental] relation. This is done by replacing the [incremental] relation with the [relations]
 * relation at the same index. We then take the union of all the results, and return the distinct
 * values.
 *
 * @param rule the [Rule] to evaluate.
 * @param relations the base [Relation]s to evaluate the rule against.
 * @param incremental the delta [Relation]s to evaluate the rule against.
 */
private fun Context.evalRuleIncremental(
    rule: Rule,
    relations: List<Relation>,
    incremental: List<Relation>,
): Relation {
  require(rule.clauses.size == relations.size) { "Not the same number of relations and clauses." }
  require(rule.clauses.size == incremental.size) { "Not the same number of relations and clauses." }
  var result = Relation.empty(rule.arity)
  for (i in 0 until rule.clauses.size) {
    val args = List(rule.clauses.size) { index -> if (index == i) incremental[i] else relations[i] }
    result = result.union(evalRule(rule, args))
  }
  return result.distinct()
}

/**
 * [eval] takes a [PredicateWithArity], and evaluates it against the [RulesDatabase] and the base
 * and derived [FactsDatabase], returning the values that could be derived as a new [Relation]. In
 * general, the intersection of the base and derived [FactsDatabase] is empty, but this is not
 * enforced.
 *
 * @param predicate the [PredicateWithArity] to evaluate.
 * @param idb the [RulesDatabase] to evaluate the rule against.
 * @param base the base [FactsDatabase] to evaluate the rule against.
 * @param derived the derived [FactsDatabase] to evaluate the rule against.
 * @return the [Relation] that could be derived from the [RulesDatabase] and the [FactsDatabase].
 */
private fun Context.eval(
    predicate: PredicateWithArity,
    idb: RulesDatabase,
    base: FactsDatabase,
    derived: FactsDatabase,
): Relation {
  val rules = idb[predicate]
  var result = Relation.empty(predicate.arity)
  val facts = base + derived
  for (rule in rules) {
    val list = rule.clauses.map { facts[PredicateWithArity(it.predicate, it.arity)] }
    result = result.union(evalRule(rule, list))
  }
  return result.distinct()
}

/**
 * [evalIncremental] takes one [PredicateWithArity], and evaluates it against the [RulesDatabase],
 * base [FactsDatabase], and delta [FactsDatabase], returning the values that could be derived as a
 * new [Relation].
 *
 * @param predicate the [PredicateWithArity] to evaluate.
 * @param idb the [RulesDatabase] to evaluate the rule against.
 * @param base the base [FactsDatabase] to evaluate the rule against.
 * @param derived the derived [FactsDatabase] to evaluate the rule against.
 * @param delta the delta [FactsDatabase] to evaluate the rule against.
 */
private fun Context.evalIncremental(
    predicate: PredicateWithArity,
    idb: RulesDatabase,
    base: FactsDatabase,
    derived: FactsDatabase,
    delta: FactsDatabase,
): Relation {
  val rules = idb[predicate]
  var result = Relation.empty(predicate.arity)
  val factsBase = base + derived
  val factsDelta = base + delta // Negation needs base facts to be present in the delta.
  for (rule in rules) {
    val baseList = rule.clauses.map { factsBase[PredicateWithArity(it.predicate, it.arity)] }
    val deltaList = rule.clauses.map { factsDelta[PredicateWithArity(it.predicate, it.arity)] }
    result = result.union(evalRuleIncremental(rule, baseList, deltaList))
  }
  return result.distinct()
}

/** [naiveEval] takes an [RulesDatabase] and a base [FactsDatabase], and derives new facts. */
internal fun Context.naiveEval(idb: RulesDatabase, base: FactsDatabase): FactsDatabase {
  val rels = MutableFactsDatabase.empty()
  val copy = MutableFactsDatabase.empty()

  do {
    for (predicate in idb) copy[predicate] = rels[predicate]
    for (predicate in idb) rels[predicate] = eval(predicate, idb, base, rels)
  } while (rels != copy)

  return rels
}

/** [semiNaiveEval] takes an [RulesDatabase] and a base [FactsDatabase], and derives new facts. */
internal fun Context.semiNaiveEval(idb: RulesDatabase, edb: FactsDatabase): FactsDatabase {
  val rels = MutableFactsDatabase.empty()
  val delta = MutableFactsDatabase.empty()
  val copy = MutableFactsDatabase.empty()

  for (predicate in idb) {
    val res = eval(predicate, idb, edb, MutableFactsDatabase.empty())
    rels[predicate] = res
    delta[predicate] = res
  }

  do {
    for (p in idb) copy[p] = delta[p]
    for (p in idb) {
      val facts = evalIncremental(p, idb, edb, rels, copy)
      val existing = rels[p]
      delta[p] = facts - existing
    }
    rels += delta
  } while (delta.isNotEmpty())

  return rels
}

/**
 * [stratifiedEval] performs stratified evaluation of the rules, and returns the resulting
 * [FactsDatabase].
 *
 * @param idb the [RulesDatabase] to evaluate.
 * @param edb the [FactsDatabase] to evaluate.
 * @param evalStrata the evaluator to use for each stratum.
 * @return the resulting [FactsDatabase].
 */
internal fun stratifiedEval(
    idb: RulesDatabase,
    edb: FactsDatabase,
    evalStrata: (RulesDatabase, FactsDatabase) -> FactsDatabase,
): FactsDatabase {
  // 1. Compute the different strata using the rules.
  // 2. Use a topological sort to order the strata for evaluation.
  // 3. Evaluate each stratum using the evalStrata evaluator.
  // 4. Return the union of all the strata.
  val rulesToVertices = mutableMapOf<PredicateWithArity, Vertex>()
  val verticesToRules = mutableMapOf<Vertex, PredicateWithArity>()
  val graph = buildDirectedGraph {
    for (id in idb) {
      val vertex = addVertex()
      rulesToVertices[id] = vertex
      verticesToRules[vertex] = id
    }
    for (id in idb) {
      val rules = idb[id]
      for (rule in rules) {
        for (clause in rule.clauses) {
          val fromKey = PredicateWithArity(clause.predicate, clause.arity)
          val toKey = PredicateWithArity(rule.predicate, rule.arity)
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

  val order =
      scc.topologicalSort().toTypedArray().map { vertex -> strata[vertex] ?: error("No stratum") }

  // Evaluate the different strata in order.
  var result = edb
  for (stratum in order) {
    val rules = idb.filter(stratum)
    result += evalStrata(rules, result)
  }
  return result
}
