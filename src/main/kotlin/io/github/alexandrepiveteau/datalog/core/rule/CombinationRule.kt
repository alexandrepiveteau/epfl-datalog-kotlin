package io.github.alexandrepiveteau.datalog.core.rule

/**
 * A [CombinationRule] defines the derivation of a new relation, following the pattern defined by
 * the [List] of [Atom]s for production and using the [BodyLiteral]s for the derivation.
 *
 * @param head the [HeadLiteral] that should be produced by the [CombinationRule].
 * @param body the [List] of [BodyLiteral]s that should be used to derive some new facts.
 */
data class CombinationRule<out T>(
    override val head: HeadLiteral<T>,
    override val body: List<BodyLiteral<T>>,
) : Rule<T>
