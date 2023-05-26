package io.github.alexandrepiveteau.datalog.core.rule

/**
 * A class representing a variable [Atom].
 *
 * @param T the type of the elements in the relation.
 * @param id the unique identifier of the variable, which gives its identity to the variable.
 */
data class Variable<out T>(val id: Int) : Atom<T>
