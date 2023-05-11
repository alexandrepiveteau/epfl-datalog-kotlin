package io.github.alexandrepiveteau.datalog.dsl

/**
 * A [Term] which contains a list of [Atom]s. The term might be negated.
 *
 * @param T the type of the elements in the relation.
 * @param relation the relation of this term.
 * @param atoms the atoms in this term.
 * @param negated true iff this term is negated.
 */
data class Term<out T>(val relation: Relation<T>, val atoms: List<Atom<T>>, val negated: Boolean)
