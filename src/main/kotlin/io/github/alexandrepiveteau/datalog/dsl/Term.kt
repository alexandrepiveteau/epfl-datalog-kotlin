package io.github.alexandrepiveteau.datalog.dsl

import io.github.alexandrepiveteau.datalog.core.Predicate

/**
 * A [Term] which contains a list of [Atom]s. The term might be negated.
 *
 * @param T the type of the elements in the relation.
 * @param predicate the predicate of this term.
 * @param atoms the atoms in this term.
 * @param negated true iff this term is negated.
 */
data class Term<out T>(val predicate: Predicate, val atoms: List<Atom<T>>, val negated: Boolean)
