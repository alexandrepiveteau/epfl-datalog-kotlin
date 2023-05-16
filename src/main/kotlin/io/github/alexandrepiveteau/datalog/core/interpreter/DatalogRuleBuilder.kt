package io.github.alexandrepiveteau.datalog.core.interpreter

import io.github.alexandrepiveteau.datalog.core.Atom
import io.github.alexandrepiveteau.datalog.core.AtomList
import io.github.alexandrepiveteau.datalog.core.Predicate
import io.github.alexandrepiveteau.datalog.core.RuleBuilder

/** An implementation of [RuleBuilder] which can be used to build [PredicateRule]s. */
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
      val column: Atom,
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
      column: Atom,
      result: Atom
  ) {
    // TODO : Support this and produce appropriate rules.
    aggregates.add(StoredAggregate(operator, same, column, result))
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
  fun toRule(predicate: Predicate, atoms: AtomList): PredicateRule {
    if (aggregates.isNotEmpty())
        throw UnsupportedOperationException("Aggregates are not supported yet.")
    val clauses = predicates.map { PredicateClause(it.predicate, it.atoms, it.negated) }
    return PredicateRule(predicate, atoms, clauses)
  }
}
