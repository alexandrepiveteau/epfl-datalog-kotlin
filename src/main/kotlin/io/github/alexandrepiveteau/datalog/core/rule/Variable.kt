package io.github.alexandrepiveteau.datalog.core.rule

/**
 * A class representing a variable [Atom].
 *
 * @param id the unique identifier of the variable, which gives its identity to the variable.
 */
data class Variable(val id: Any) : Atom<Nothing>
