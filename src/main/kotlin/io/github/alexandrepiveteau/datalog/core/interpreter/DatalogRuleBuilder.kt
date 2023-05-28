package io.github.alexandrepiveteau.datalog.core.interpreter

import io.github.alexandrepiveteau.datalog.core.RuleBuilder
import io.github.alexandrepiveteau.datalog.core.rule.*

/** An implementation of [RuleBuilder] which can be used to build [CombinationRule]s. */
internal class DatalogRuleBuilder<T> : RuleBuilder<T> {

  /** A predicate which has been stored. */
  private data class StoredPredicate<out T>(
      val predicate: Predicate,
      val atoms: List<Atom<T>>,
      val negated: Boolean,
  )

  /** The list of [StoredPredicate]s. */
  private val predicates = mutableSetOf<StoredPredicate<T>>()

  /** The list of [Aggregate]s. */
  private val aggregates = mutableSetOf<Aggregate>()

  override fun predicate(
      predicate: Predicate,
      atoms: List<Atom<T>>,
      negated: Boolean,
  ) {
    predicates.add(StoredPredicate(predicate, atoms, negated))
  }

  override fun aggregate(
      operator: RuleBuilder.AggregationFunction,
      same: Collection<Variable>,
      columns: Collection<Variable>,
      result: Variable,
  ) {
    aggregates.add(Aggregate(operator, same, columns, result))
  }

  /**
   * Returns the [Rule] that has been built using the [predicate] and [aggregate] functions.
   * Depending on the clauses, the resulting rule might be an aggregate rule, or a regular predicate
   * rule.
   *
   * If no valid rule can be built, an [IllegalStateException] will be thrown.
   *
   * @param predicate the predicate of the head of the rule.
   * @param atoms the [List] of [Atom]s of the head of the rule.
   */
  fun toRule(predicate: Predicate, atoms: List<Atom<T>>): Rule<T> {
    val rule =
        if (aggregates.isEmpty()) {
          toCombinationRule(predicate, atoms)
        } else {
          toAggregationRule(predicate, atoms)
        }
    return rule
  }

  /** @see toRule */
  private fun toCombinationRule(predicate: Predicate, atoms: List<Atom<T>>): CombinationRule<T> {
    require(aggregates.isEmpty()) { "Aggregates should be empty for combination rules." }
    val clauses = predicates.map { BodyLiteral(it.predicate, it.atoms, it.negated) }
    return CombinationRule(
        head = HeadLiteral(predicate, atoms),
        body = clauses,
    )
  }

  /** @see toRule */
  private fun toAggregationRule(predicate: Predicate, atoms: List<Atom<T>>): AggregationRule<T> {
    require(aggregates.size == 1) { "Aggregated rules should must have exactly one aggregate." }
    require(predicates.size == 1) { "Aggregated rules should must have exactly one clause." }
    val aggregate = aggregates.first()
    val clause = predicates.first().let { BodyLiteral(it.predicate, it.atoms, it.negated) }
    return AggregationRule(
        head = HeadLiteral(predicate, atoms),
        clause = clause,
        aggregate = aggregate,
    )
  }
}
