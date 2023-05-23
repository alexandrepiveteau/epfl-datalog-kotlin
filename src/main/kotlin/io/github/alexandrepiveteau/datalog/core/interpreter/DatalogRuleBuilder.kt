package io.github.alexandrepiveteau.datalog.core.interpreter

import io.github.alexandrepiveteau.datalog.core.Atom
import io.github.alexandrepiveteau.datalog.core.AtomList
import io.github.alexandrepiveteau.datalog.core.Predicate
import io.github.alexandrepiveteau.datalog.core.RuleBuilder

/** An implementation of [RuleBuilder] which can be used to build [CombinationRule]s. */
internal class DatalogRuleBuilder : RuleBuilder {

  /** A predicate which has been stored. */
  private data class StoredPredicate(
      val predicate: Predicate,
      val atoms: AtomList,
      val negated: Boolean,
  )

  /** An aggregate which has been stored. */
  private data class StoredAggregate(
      val operator: RuleBuilder.Aggregate,
      val same: AtomList,
      val columns: AtomList,
      val result: Atom,
  )

  /** The list of [StoredPredicate]s. */
  private val predicates = mutableSetOf<StoredPredicate>()

  /** The list of [StoredAggregate]. */
  private val aggregates = mutableSetOf<StoredAggregate>()

  override fun predicate(
      predicate: Predicate,
      atoms: AtomList,
      negated: Boolean,
  ) {
    predicates.add(StoredPredicate(predicate, atoms, negated))
  }

  override fun aggregate(
      operator: RuleBuilder.Aggregate,
      same: AtomList,
      columns: AtomList,
      result: Atom,
  ) {
    aggregates.add(StoredAggregate(operator, same, columns, result))
  }

  /**
   * Returns the [Rule] that has been built using the [predicate] and [aggregate] functions.
   * Depending on the clauses, the resulting rule might be an aggregate rule, or a regular predicate
   * rule.
   *
   * If no valid rule can be built, an [IllegalStateException] will be thrown.
   *
   * @param predicate the predicate of the head of the rule.
   * @param atoms the [AtomList] of the head of the rule.
   */
  fun toRule(predicate: Predicate, atoms: AtomList): Rule {
    return if (aggregates.isEmpty()) {
      toCombinationRule(predicate, atoms)
    } else {
      toAggregationRule(predicate, atoms)
    }
  }

  /** @see toRule */
  private fun toCombinationRule(predicate: Predicate, atoms: AtomList): CombinationRule {
    require(aggregates.isEmpty()) { "Aggregates should be empty for combination rules." }
    val clauses = predicates.map { Clause(it.predicate, it.atoms, it.negated) }
    return CombinationRule(
        predicate = predicate,
        atoms = atoms,
        clauses = clauses,
    )
  }

  /** @see toRule */
  private fun toAggregationRule(predicate: Predicate, atoms: AtomList): AggregationRule {
    require(aggregates.size == 1) { "Aggregated rules should must have exactly one aggregate." }
    require(predicates.size == 1) { "Aggregated rules should must have exactly one clause." }
    val aggregate = aggregates.first()
    val clause = predicates.first().let { Clause(it.predicate, it.atoms, it.negated) }
    return AggregationRule(
        predicate = predicate,
        atoms = atoms,
        clause = clause,
        operator = aggregate.operator,
        same = aggregate.same,
        columns = aggregate.columns,
        result = aggregate.result,
    )
  }
}
