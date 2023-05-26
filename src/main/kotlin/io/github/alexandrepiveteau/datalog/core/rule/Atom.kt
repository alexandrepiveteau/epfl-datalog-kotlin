package io.github.alexandrepiveteau.datalog.core.rule

/**
 * An interface representing an atom in Datalog. An atom can either be a [Value] or [Variable].
 *
 * @param T the type of the elements in the relation.
 */
sealed interface Atom<out T>
