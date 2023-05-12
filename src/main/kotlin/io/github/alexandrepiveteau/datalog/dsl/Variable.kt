package io.github.alexandrepiveteau.datalog.dsl

import io.github.alexandrepiveteau.datalog.core.Atom as CoreAtom

/**
 * A class representing a variable [Atom].
 *
 * @param T the type of the elements in the relation.
 * @param atom the unique identifier of the variable, which gives its identity to the variable.
 */
data class Variable<out T>(val atom: CoreAtom) : Atom<T>
