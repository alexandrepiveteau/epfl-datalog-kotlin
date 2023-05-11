package io.github.alexandrepiveteau.datalog.core.interpreter

import io.github.alexandrepiveteau.datalog.core.*
import io.github.alexandrepiveteau.datalog.core.Relation as CoreRelation
import io.github.alexandrepiveteau.datalog.core.algebra.*
import io.github.alexandrepiveteau.datalog.core.solver.Rule

/**
 * Returns the [Set] of all the indices of the variables in the [Rule]. This is used to generate the
 * selection clauses.
 */
private fun variableIndices(rule: AtomList): Set<List<Int>> {
  val variables = mutableMapOf<Atom, MutableList<Int>>()
  rule.forEachIndexed { index, atom ->
    if (atom.isVariable) variables.getOrPut(atom) { mutableListOf() }.add(index)
  }
  return variables.values.map { it.toList() }.toSet()
}

/**
 * Returns the [List] of indices for constants in the [Rule]. This is used to generate the clauses
 * that filter the constants.
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
      Column.Index(index)
    }
    Column.Constant(atom)
  }
}

/** The extensional database is the set of facts that have already been derived. */
typealias EDB = Map<CoreRelation, Relation>

/** The intentional database is the set of rules that may be used to derive new facts. */
typealias IDB = Map<CoreRelation, Set<Rule>>

/**
 * [evalRule] takes one [Rule], and evaluates it against a list of [Relation]s, returning the values
 * that could be derived as a new [Relation].
 *
 * @param rule the [Rule] to evaluate.
 * @param edb the [EDB] to evaluate the rule against.
 */
private fun evalRule(rule: Rule, edb: EDB): Relation {
  // 1. Find all the variables in the rule, and their indices.
  // 2. Find all the constants in the rule.
  // 3. Generate all the selection clauses.
  // 4. Perform the join and apply the selection clauses.
  // 5. Perform a projection to remap the indices.

  val relations = rule.clauses.map { edb[it.relation] ?: Relation.empty(it.atoms.size) }
  val concat = rule.clauses.flatMap { it.atoms.toList() }.asAtomList()
  val variables = variableIndices(concat)
  val constants = constantIndices(concat)
  val projection = projection(rule.atoms, concat)

  // Join, then filter, then project.
  var result = relations.join()

  // Filter the variable indices.
  result =
      result.select {
        for (indices in variables) {
          indices.zipWithNext().forEach { (a, b) -> if (it[a] != it[b]) return@select false }
        }
        return@select true
      }

  // Filter the constants.
  result =
      result.select {
        for (index in constants) {
          if (it[index] != concat[index]) return@select false
        }
        return@select true
      }

  // Project the columns.
  return result.project(projection)
}

/**
 * [eval] takes one [CoreRelation], and evaluates it against a list of [Relation]s, returning the
 * values that could be derived as a new [Relation].
 *
 * This will throw an [IllegalStateException] if the [CoreRelation] is not present in the [IDB], or
 * if there are no derivation rules for the [CoreRelation].
 *
 * @param predicate the [CoreRelation] to evaluate.
 * @param idb the [IDB] to evaluate the rule against.
 * @param edb the [EDB] to evaluate the rule against.
 */
private fun eval(predicate: CoreRelation, idb: IDB, edb: EDB): Relation {
  val rules = idb[predicate] ?: error("No rules for $predicate")
  var result = Relation.empty(rules.first().atoms.size)
  for (rule in rules) {
    result = result.union(evalRule(rule, edb))
  }
  return result.distinct()
}

/** Performs the union between two [EDB]. */
private operator fun EDB.plus(other: EDB): EDB = buildMap {
  forEach { (id, rel) ->
    val o = other[id] ?: Relation.empty(rel.arity)
    put(id, rel.union(o))
  }
  other.forEach { (id, rel) -> if (!containsKey(id)) put(id, rel) }
}

/**
 * [naiveEval] takes a list of [Rule]s as an intentional database, and evaluates them against a list
 * of facts to produce a new set of facts.
 */
fun naiveEval(idb: IDB, edb: EDB): EDB {
  val relations =
      idb.mapValuesTo(mutableMapOf()) { (_, rules) ->
        val arity = rules.first().atoms.size
        Relation.empty(arity)
      }

  val updated =
      idb.mapValuesTo(mutableMapOf()) { (_, rules) ->
        val arity = rules.first().atoms.size
        Relation.empty(arity)
      }

  do {
    for ((id, relation) in relations) updated[id] = relation
    for ((id, _) in relations) {
      relations[id] = eval(id, idb, relations + edb)
    }
  } while (relations != updated)

  return relations + edb
}
