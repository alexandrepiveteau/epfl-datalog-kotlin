package io.github.alexandrepiveteau.datalog.core.rule

/**
 * A [Literal] which defines the head of a rule.
 *
 * @param predicate the [Predicate] of the clause.
 * @param atoms the pattern that should be matched.
 */
data class HeadLiteral<out T>(
    override val predicate: Predicate,
    override val atoms: List<Atom<T>>,
) : Literal<T>
