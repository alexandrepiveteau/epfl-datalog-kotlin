package io.github.alexandrepiveteau.datalog.core.interpreter

import io.github.alexandrepiveteau.datalog.core.interpreter.algebra.*
import io.github.alexandrepiveteau.datalog.core.interpreter.algebra.Column.Constant
import io.github.alexandrepiveteau.datalog.core.interpreter.algebra.Column.Index
import io.github.alexandrepiveteau.datalog.core.interpreter.database.*
import io.github.alexandrepiveteau.datalog.core.rule.*

/**
 * Returns the [Set] of all the indices of the variables in the [CombinationRule]. This is used to
 * generate the selection clauses.
 */
private fun <T> variableIndices(rule: List<Atom<T>>): Set<List<Int>> {
  val variables = mutableMapOf<Variable<T>, MutableList<Int>>()
  rule.forEachIndexed { index, atom ->
    when (atom) {
      is Variable -> variables.getOrPut(atom) { mutableListOf() }.add(index)
      is Value -> Unit
    }
  }
  return variables.values.map { it.toList() }.toSet()
}

/**
 * Returns the [List] of indices for constants in the [CombinationRule]. This is used to generate
 * the clauses that filter the constants.
 */
private fun constantIndices(rule: List<Atom<*>>): List<Int> {
  val constants = mutableListOf<Int>()
  rule.forEachIndexed { index, atom ->
    when (atom) {
      is Variable -> Unit
      is Value -> constants.add(index)
    }
  }
  return constants
}

/**
 * Returns the [List] of [Column]s that should be used for the projection. This is used to generate
 * the projection clauses.
 */
private fun <T> projection(predicate: List<Atom<T>>, rule: List<Atom<T>>): List<Column<T>> {
  return predicate.map { atom ->
    when (atom) {
      is Variable -> Index(rule.indexOfFirst { it == atom })
      is Value -> Constant(atom)
    }
  }
}

/** Returns the [Set] of [Set] of [Column] that should be used for equality selection. */
private fun <T> selection(rule: List<Atom<T>>): Set<Set<Column<T>>> {
  return buildSet {
    val constants = constantIndices(rule)
    for (variable in variableIndices(rule)) {
      add(variable.mapTo(mutableSetOf()) { Index(it) })
    }
    for (constant in constants) {
      // TODO : Remove this unsafe cast somehow.
      add(setOf(Constant(rule[constant] as Value<T>), Index(constant)))
    }
  }
}

/**
 * [evalRule] takes a [Rule] and a list of [Relation]s, and evaluates it against the list of
 * [Relation]s, returning the values that could be derived as a new [Relation].
 *
 * The rule must have exactly one clause for each [Relation].
 *
 * @param rule the [Rule] to evaluate.
 * @param relations the [Relation]s to evaluate the rule against.
 */
private fun <T> Context<T>.evalRule(rule: Rule<T>, relations: List<Relation<T>>): Relation<T> {
  return when (rule) {
    is CombinationRule -> evalCombinationRule(rule, relations)
    is AggregationRule -> evalAggregationRule(rule, relations.single())
  }
}

/** @see evalRule */
private fun <T> Context<T>.evalCombinationRule(
    rule: CombinationRule<T>,
    relations: List<Relation<T>>
): Relation<T> {
  require(rule.body.size == relations.size) { "Not the same number of relations and clauses." }
  // 1. Negate all the relations that are negated in the rule.
  // 2. Generate a concatenation of all atoms in the rule, after the join.
  // 3. Join all the relations.
  // 4. Select the rows that match the constants and variables.
  // 5. Project the rows to the correct indices, and add constants to the projection.
  val negated = relations.mapIndexed { idx, r -> if (rule.body[idx].negated) r.negated() else r }
  val concat = rule.body.flatMap { it.atoms.toList() }
  return negated.join().select(selection(concat)).project(projection(rule.head.atoms, concat))
}

/** @see evalRule */
private fun <T> Context<T>.evalAggregationRule(
    rule: AggregationRule<T>,
    relation: Relation<T>,
): Relation<T> {
  // 1. Negate the relation if the rule is negated.
  // 2. Perform the aggregation.
  val negated = if (rule.clause.negated) relation.negated() else relation
  val projection =
      rule.head.atoms.map { atom ->
        when (atom) {
          is Variable ->
              if (atom == rule.result) AggregationColumn.Aggregate
              else AggregationColumn.Column(Index(rule.clause.atoms.indexOf(atom)))
          is Value -> AggregationColumn.Column(Constant(atom))
        }
      }
  val same = rule.same.mapTo(mutableSetOf()) { Index(rule.clause.atoms.indexOf(it)) }
  val indices = rule.columns.mapTo(mutableSetOf()) { Index(rule.clause.atoms.indexOf(it)) }
  return negated.aggregate(
      projection = projection,
      same = same,
      domain = domain,
      aggregate = rule.operator,
      indices = indices,
  )
}

/**
 * [evalRuleIncremental] takes a [CombinationRule] and two sets of [Relation]s, and evaluates them
 * against the list of [relations] and [incremental] relations, returning the values that could be
 * derived as a new [Relation].
 *
 * For each [incremental] relation, we evaluate the rule with all the [relations] relations and one
 * [incremental] relation. This is done by replacing the [incremental] relation with the [relations]
 * relation at the same index. We then take the union of all the results, and return the distinct
 * values.
 *
 * @param rule the [CombinationRule] to evaluate.
 * @param relations the base [Relation]s to evaluate the rule against.
 * @param incremental the delta [Relation]s to evaluate the rule against.
 */
private fun <T> Context<T>.evalRuleIncremental(
    rule: Rule<T>,
    relations: List<Relation<T>>,
    incremental: List<Relation<T>>,
): Relation<T> {
  require(rule.body.size == relations.size) { "Not the same number of relations and clauses." }
  require(rule.body.size == incremental.size) { "Not the same number of relations and clauses." }
  var result = Relation.empty<T>(rule.head.arity)
  for (i in 0 until rule.body.size) {
    val args = List(rule.body.size) { index -> if (index == i) incremental[i] else relations[i] }
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
private fun <T> Context<T>.eval(
    predicate: PredicateWithArity,
    idb: RulesDatabase<T>,
    base: FactsDatabase<T>,
    derived: FactsDatabase<T>,
): Relation<T> {
  val rules = idb[predicate]
  var result = Relation.empty<T>(predicate.arity)
  val facts = base + derived
  for (rule in rules) {
    val list = rule.body.map { facts[PredicateWithArity(it.predicate, it.arity)] }
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
private fun <T> Context<T>.evalIncremental(
    predicate: PredicateWithArity,
    idb: RulesDatabase<T>,
    base: FactsDatabase<T>,
    derived: FactsDatabase<T>,
    delta: FactsDatabase<T>,
): Relation<T> {
  val rules = idb[predicate]
  var result = Relation.empty<T>(predicate.arity)
  val factsBase = base + derived
  val factsDelta = base + delta // Negation needs base facts to be present in the delta.
  for (rule in rules) {
    val baseList = rule.body.map { factsBase[PredicateWithArity(it.predicate, it.arity)] }
    val deltaList = rule.body.map { factsDelta[PredicateWithArity(it.predicate, it.arity)] }
    result = result.union(evalRuleIncremental(rule, baseList, deltaList))
  }
  return result.distinct()
}

/** [naiveEval] takes an [RulesDatabase] and a base [FactsDatabase], and derives new facts. */
internal fun <T> Context<T>.naiveEval(
    idb: RulesDatabase<T>,
    base: FactsDatabase<T>,
): FactsDatabase<T> {
  val rels = MutableFactsDatabase.empty<T>()
  val copy = MutableFactsDatabase.empty<T>()

  do {
    for (predicate in idb) copy[predicate] = rels[predicate]
    for (predicate in idb) rels[predicate] = eval(predicate, idb, base, rels)
  } while (rels != copy)

  return rels
}

/** [semiNaiveEval] takes an [RulesDatabase] and a base [FactsDatabase], and derives new facts. */
internal fun <T> Context<T>.semiNaiveEval(
    idb: RulesDatabase<T>,
    edb: FactsDatabase<T>,
): FactsDatabase<T> {
  val rels = MutableFactsDatabase.empty<T>()
  val delta = MutableFactsDatabase.empty<T>()
  val copy = MutableFactsDatabase.empty<T>()

  for (predicate in idb) {
    val res = eval(predicate, idb, edb, MutableFactsDatabase.empty<T>())
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
