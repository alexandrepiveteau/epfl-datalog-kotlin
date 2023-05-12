package io.github.alexandrepiveteau.datalog.core.interpreter

import io.github.alexandrepiveteau.datalog.core.*
import io.github.alexandrepiveteau.datalog.core.interpreter.algebra.*
import io.github.alexandrepiveteau.datalog.core.interpreter.algebra.Column.Constant
import io.github.alexandrepiveteau.datalog.core.interpreter.algebra.Column.Index
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
private fun Context.evalRule(rule: Rule, vararg relations: Relation): Relation {
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
 * [eval] takes one [Predicate], and evaluates it against the [IDB] and [EDB], returning the values
 * that could be derived as a new [Relation].
 *
 * @param predicate the [Predicate] to evaluate.
 * @param idb the [IDB] to evaluate the rule against.
 * @param edb the [EDB] to evaluate the rule against.
 */
private fun Context.eval(predicate: Predicate, idb: IDB, edb: EDB): Relation {
  val rules = idb[predicate]
  var result = Relation.empty(idb.arity(predicate))
  for (rule in rules) {
    val list = rule.clauses.map { edb[it.predicate] ?: Relation.empty(it.arity) }
    result = result.union(evalRule(rule, *list.toTypedArray()))
  }
  return result.distinct()
}

/**
 * [naiveEval] takes a list of [PredicateRule]s as an intentional database, and evaluates them
 * against a list of facts to produce a new set of facts.
 */
internal fun Context.naiveEval(idb: IDB, edb: EDB): EDB {
  val relations = edb.toMutableEDB()
  val updated = edb.toMutableEDB()

  do {
    for (predicate in idb) {
      updated[predicate] = relations[predicate] ?: Relation.empty(idb.arity(predicate))
    }
    for (predicate in idb) {
      relations[predicate] = eval(predicate, idb, relations)
    }
  } while (relations != updated)

  return relations + edb
}

/**
 * [stratifiedEval] performs stratified evaluation of the rules, and returns the resulting [EDB].
 *
 * @param idb the [IDB] to evaluate.
 * @param edb the [EDB] to evaluate.
 * @param evalStrata the evaluator to use for each stratum.
 * @return the resulting [EDB].
 */
internal fun stratifiedEval(
    idb: IDB,
    edb: EDB,
    evalStrata: (IDB, EDB) -> EDB,
): EDB {
  // 1. Compute the different strata using the rules.
  // 2. Use a topological sort to order the strata for evaluation.
  // 3. Evaluate each stratum using the evalStrata evaluator.
  // 4. Return the union of all the strata.
  val rulesToVertices = mutableMapOf<Predicate, Vertex>()
  val verticesToRules = mutableMapOf<Vertex, Predicate>()
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
          if (clause.predicate in idb) {
            val from =
                rulesToVertices[clause.predicate] ?: error("No vertex for ${clause.predicate}")
            val to = rulesToVertices[rule.predicate] ?: error("No vertex for ${rule.predicate}")
            addArc(from arcTo to)
          }
        }
      }
    }
  }
  val (scc, map) = graph.stronglyConnectedComponentsKosaraju()
  val strata = mutableMapOf<Vertex, MutableSet<Predicate>>()

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
    result = evalStrata(rules, result)
  }
  return result
}
