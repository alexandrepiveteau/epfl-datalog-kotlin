package io.github.alexandrepiveteau.datalog.dsl

/**
 * An interface representing an atom in Datalog. An atom can either be a [Value] or [Variable], and
 * may appear in a [Term] when it is used as a predicate argument.
 *
 * @param T the type of the elements in the relation.
 */
sealed interface Atom<out T>
