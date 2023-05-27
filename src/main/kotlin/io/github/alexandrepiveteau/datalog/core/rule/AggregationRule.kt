package io.github.alexandrepiveteau.datalog.core.rule

/**
 * An [AggregationRule] defines the derivation of a new relation, following the pattern defined by
 * the [List] of [Atom]s for production and using the given [BodyLiteral] for derivation.
 *
 * @param head the [HeadLiteral] that should be produced by the [AggregationRule].
 * @param clause the [BodyLiteral] that should be used to derive some new facts.
 * @param aggregate the [Aggregate] that should be used to aggregate the [clause].
 */
data class AggregationRule<out T>(
    override val head: HeadLiteral<T>,
    val clause: BodyLiteral<T>,
    val aggregate: Aggregate,
) : Rule<T> {

  override val body: List<BodyLiteral<T>>
    get() = listOf(clause)
}
