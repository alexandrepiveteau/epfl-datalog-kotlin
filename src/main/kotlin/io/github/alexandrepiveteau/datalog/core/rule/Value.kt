package io.github.alexandrepiveteau.datalog.core.rule

/**
 * A class representing a value [Atom].
 *
 * @param T the type of the elements in the relation.
 * @param value the value of the atom.
 */
data class Value<out T>(val value: T) : Atom<T>
