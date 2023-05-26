package io.github.alexandrepiveteau.datalog.core.rule

import io.github.alexandrepiveteau.datalog.core.RuleBuilder

/**
 * An [AggregationRule] defines the derivation of a new relation, following the pattern defined by
 * the [List] of [Atom]s for production and using the given [BodyLiteral] for derivation.
 *
 * @param head the [HeadLiteral] that should be produced by the [AggregationRule].
 * @param clause the [BodyLiteral] that should be used to derive some new facts.
 * @param operator the [RuleBuilder.Aggregate] operator that should be used to aggregate the [same]
 *   atoms.
 * @param same the [List] of [Atom]s that should be used to aggregate the [columns] atoms.
 * @param columns the variable [Atom] that should be aggregated.
 * @param result the variable [Atom] that should be used to store the result of the aggregation.
 */
data class AggregationRule<out T>(
    override val head: HeadLiteral<T>,
    val clause: BodyLiteral<T>,
    val operator: RuleBuilder.Aggregate,
    val same: Collection<Variable>,
    val columns: Collection<Variable>,
    val result: Variable,
) : Rule<T> {

  override val body: List<BodyLiteral<T>>
    get() = listOf(clause)
}
