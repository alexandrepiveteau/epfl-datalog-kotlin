package io.github.alexandrepiveteau.datalog.core.rule

/**
 * A [BodyLiteral] defines which parts of a relation should be matched when deriving some new facts.
 * The [atoms] define the pattern that should be matched, and the [negated] flag indicates whether
 * the pattern should be negated or not.
 *
 * @param predicate the [Predicate] of the clause.
 * @param atoms the pattern that should be matched.
 * @param negated true if the pattern should be negated, false otherwise
 */
data class BodyLiteral<out T>(
    override val predicate: Predicate,
    override val atoms: List<Atom<T>>,
    val negated: Boolean,
) : Literal<T>
