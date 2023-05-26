package io.github.alexandrepiveteau.datalog.core.interpreter

import io.github.alexandrepiveteau.datalog.core.Predicate
import io.github.alexandrepiveteau.datalog.core.RuleBuilder
import io.github.alexandrepiveteau.datalog.dsl.Atom
import io.github.alexandrepiveteau.datalog.dsl.Variable

/** An implementation of [RuleBuilder] which can be used to build [CombinationRule]s. */
internal class DatalogRuleBuilder<T> : RuleBuilder<T> {

  /** A predicate which has been stored. */
  private data class StoredPredicate<out T>(
      val predicate: Predicate,
      val atoms: List<Atom<T>>,
      val negated: Boolean,
  )

  /** An aggregate which has been stored. */
  private data class StoredAggregate<out T>(
      val operator: RuleBuilder.Aggregate,
      val same: Collection<Variable<T>>,
      val columns: Collection<Variable<T>>,
      val result: Variable<T>,
  )

  /** The list of [StoredPredicate]s. */
  private val predicates = mutableSetOf<StoredPredicate<T>>()

  /** The list of [StoredAggregate]. */
  private val aggregates = mutableSetOf<StoredAggregate<T>>()

  override fun predicate(
      predicate: Predicate,
      atoms: List<Atom<T>>,
      negated: Boolean,
  ) {
    predicates.add(StoredPredicate(predicate, atoms, negated))
  }

  override fun aggregate(
      operator: RuleBuilder.Aggregate,
      same: Collection<Variable<T>>,
      columns: Collection<Variable<T>>,
      result: Variable<T>,
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
  fun toRule(predicate: Predicate, atoms: List<Atom<T>>): Rule<T> {
    return if (aggregates.isEmpty()) {
      toCombinationRule(predicate, atoms)
    } else {
      toAggregationRule(predicate, atoms)
    }
  }

  /** @see toRule */
  private fun toCombinationRule(predicate: Predicate, atoms: List<Atom<T>>): CombinationRule<T> {
    require(aggregates.isEmpty()) { "Aggregates should be empty for combination rules." }
    val clauses = predicates.map { Clause(it.predicate, it.atoms, it.negated) }
    return CombinationRule(
        predicate = predicate,
        atoms = atoms,
        clauses = clauses,
    )
  }

  /** @see toRule */
  private fun toAggregationRule(predicate: Predicate, atoms: List<Atom<T>>): AggregationRule<T> {
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
