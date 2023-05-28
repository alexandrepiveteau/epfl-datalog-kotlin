package io.github.alexandrepiveteau.datalog.core.interpreter

import io.github.alexandrepiveteau.datalog.core.interpreter.algebra.AggregationColumn
import io.github.alexandrepiveteau.datalog.core.interpreter.algebra.Column
import io.github.alexandrepiveteau.datalog.core.interpreter.algebra.Column.Constant
import io.github.alexandrepiveteau.datalog.core.interpreter.algebra.Column.Index
import io.github.alexandrepiveteau.datalog.core.interpreter.algebra.Relation
import io.github.alexandrepiveteau.datalog.core.interpreter.database.PredicateWithArity
import io.github.alexandrepiveteau.datalog.core.interpreter.database.RulesDatabase
import io.github.alexandrepiveteau.datalog.core.interpreter.ir.Database
import io.github.alexandrepiveteau.datalog.core.interpreter.ir.IROp
import io.github.alexandrepiveteau.datalog.core.interpreter.ir.IROp.*
import io.github.alexandrepiveteau.datalog.core.interpreter.ir.IROp.RelationalIROp.*
import io.github.alexandrepiveteau.datalog.core.rule.*

/**
 * Returns the [Set] of all the indices of the variables in the [CombinationRule]. This is used to
 * generate the selection clauses.
 */
private fun <T> variableIndices(rule: List<Atom<T>>): Set<List<Int>> {
  val variables = mutableMapOf<Variable, MutableList<Int>>()
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
private fun <T> constantIndices(rule: List<Atom<T>>): List<IndexedValue<Value<T>>> {
  return rule
      .withIndex()
      .filter { it.value is Value }
      .map { IndexedValue(it.index, it.value as Value<T>) }
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
    for ((index, value) in constants) {
      add(setOf(Constant(value), Index(index)))
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
private fun <T> Context<T>.evalRule(
    rule: Rule<T>,
    relations: List<RelationalIROp<T>>,
): RelationalIROp<T> {
  return when (rule) {
    is CombinationRule -> evalCombinationRule(rule, relations)
    is AggregationRule -> evalAggregationRule(rule, relations.single())
  }
}

/** @see evalRule */
private fun <T> Context<T>.evalCombinationRule(
    rule: CombinationRule<T>,
    relations: List<RelationalIROp<T>>
): RelationalIROp<T> {
  require(rule.body.size == relations.size) { "Not the same number of relations and clauses." }
  // 1. Negate all the relations that are negated in the rule.
  // 2. Generate a concatenation of all atoms in the rule, after the join.
  // 3. Join all the relations.
  // 4. Select the rows that match the constants and variables.
  // 5. Project the rows to the correct indices, and add constants to the projection.
  val negated = relations.mapIndexed { idx, r -> if (rule.body[idx].negated) r.negated() else r }
  val concat = rule.body.flatMap { it.atoms.toList() }
  return Project(Select(Join(negated), selection(concat)), projection(rule.head.atoms, concat))
}

/** @see evalRule */
private fun <T> Context<T>.evalAggregationRule(
    rule: AggregationRule<T>,
    relation: RelationalIROp<T>,
): RelationalIROp<T> {
  // 1. Negate the relation if the rule is negated.
  // 2. Perform the aggregation.
  val negated = if (rule.clause.negated) relation.negated() else relation
  val projection =
      rule.head.atoms.map { atom ->
        when (atom) {
          is Variable ->
              if (atom == rule.aggregate.result) AggregationColumn.Aggregate
              else AggregationColumn.Column(Index(rule.clause.atoms.indexOf(atom)))
          is Value -> AggregationColumn.Column(Constant(atom))
        }
      }
  val same = rule.aggregate.same.mapTo(mutableSetOf()) { Index(rule.clause.atoms.indexOf(it)) }
  val indices =
      rule.aggregate.columns.mapTo(mutableSetOf()) { Index(rule.clause.atoms.indexOf(it)) }
  return RelationalIROp.Aggregate(
      relation = negated,
      projection = projection,
      same = same,
      domain = domain,
      aggregate = rule.aggregate.aggregate,
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
    relations: List<RelationalIROp<T>>,
    incremental: List<RelationalIROp<T>>,
): RelationalIROp<T> {
  require(rule.body.size == relations.size) { "Not the same number of relations and clauses." }
  require(rule.body.size == incremental.size) { "Not the same number of relations and clauses." }
  var result: RelationalIROp<T> = Empty(rule.head.arity)
  for (i in 0 until rule.body.size) {
    val args = List(rule.body.size) { if (it == i) incremental[it] else relations[it] }
    result = Union(result, evalRule(rule, args))
  }
  return Distinct(result)
}

/**
 * [eval] takes a [PredicateWithArity], and evaluates it against the [RulesDatabase] and the base
 * and derived [Database], returning the values that could be derived as a new [Relation]. In
 * general, the intersection of the base and derived [Database] is empty, but this is not enforced.
 *
 * @param predicate the [PredicateWithArity] to evaluate.
 * @param idb the [RulesDatabase] to evaluate the rule against.
 * @param base the base [Database] to evaluate the rule against.
 * @param derived the derived [Database] to evaluate the rule against.
 * @return the [Relation] that could be derived from the [RulesDatabase] and the [Database].
 */
private fun <T> Context<T>.eval(
    predicate: PredicateWithArity,
    idb: RulesDatabase<T>,
    base: Database,
    derived: Database,
): RelationalIROp<T> {
  val rules = idb[predicate]
  var result: RelationalIROp<T> = Empty(predicate.arity)
  for (rule in rules) {
    val list =
        rule.body.map {
          val baseScan = Scan<T>(base, PredicateWithArity(it.predicate, it.arity))
          val derivedScan = Scan<T>(derived, PredicateWithArity(it.predicate, it.arity))
          Union(baseScan, derivedScan)
        }
    result = Union(result, evalRule(rule, list))
  }
  return Distinct(result)
}

/**
 * [evalIncremental] takes one [PredicateWithArity], and evaluates it against the [RulesDatabase],
 * base [Database], and delta [Database], returning the values that could be derived as a new
 * [Relation].
 *
 * @param predicate the [PredicateWithArity] to evaluate.
 * @param idb the [RulesDatabase] to evaluate the rule against.
 * @param base the base [Database] to evaluate the rule against.
 * @param derived the derived [Database] to evaluate the rule against.
 * @param delta the delta [Database] to evaluate the rule against.
 */
private fun <T> Context<T>.evalIncremental(
    predicate: PredicateWithArity,
    idb: RulesDatabase<T>,
    base: Database,
    derived: Database,
    delta: Database,
): RelationalIROp<T> {
  val rules = idb[predicate]
  var result: RelationalIROp<T> = Empty(predicate.arity)
  for (rule in rules) {
    val baseList =
        rule.body.map {
          val baseScan = Scan<T>(base, PredicateWithArity(it.predicate, it.arity))
          val derivedScan = Scan<T>(derived, PredicateWithArity(it.predicate, it.arity))
          Union(baseScan, derivedScan)
        }
    val deltaList =
        rule.body.map {
          // Negation needs base facts to be present in the delta.
          val baseScan = Scan<T>(base, PredicateWithArity(it.predicate, it.arity))
          val deltaScan = Scan<T>(delta, PredicateWithArity(it.predicate, it.arity))
          Union(baseScan, deltaScan)
        }
    result = Union(result, evalRuleIncremental(rule, baseList, deltaList))
  }
  return Distinct(result)
}

/** [naiveEval] takes an [RulesDatabase] and a base [Database], and derives new facts. */
internal fun <T> Context<T>.naiveEval(
    idb: RulesDatabase<T>,
    base: Database,
    result: Database,
): IROp<T> {
  val copy = Database("Copy")
  return DoWhileNotEqual(
      operation =
          Sequence(
              buildList {
                for (predicate in idb) {
                  add(Store(copy, predicate, Scan(result, predicate)))
                }
                for (predicate in idb) {
                  val res = eval(predicate, idb, base, result)
                  add(Store(result, predicate, res))
                }
              },
          ),
      first = result,
      second = copy,
  )
}

/** [semiNaiveEval] takes an [RulesDatabase] and a base [Database], and derives new facts. */
internal fun <T> Context<T>.semiNaiveEval(
    idb: RulesDatabase<T>,
    base: Database,
    result: Database,
): IROp<T> {
  val delta = Database("Delta")
  val copy = Database("Copy")

  return Sequence(
      buildList {
        for (predicate in idb) {
          val res = eval(predicate, idb, base, Database.Empty)
          add(Store(result, predicate, res))
          add(Store(delta, predicate, res))
        }

        add(
            DoWhileNotEmpty(
                operation =
                    Sequence(
                        buildList {
                          for (p in idb) add(Store(copy, p, Scan(delta, p)))
                          for (p in idb) {
                            val facts = evalIncremental(p, idb, base, result, copy)
                            val existing = Scan<T>(result, p)
                            add(Store(delta, p, Minus(facts, existing)))
                          }
                          for (p in idb) {
                            val current = Scan<T>(result, p)
                            val new = Scan<T>(delta, p)
                            add(Store(result, p, Union(current, new)))
                          }
                        }),
                database = delta,
            ),
        )
      },
  )
}
