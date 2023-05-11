package io.github.alexandrepiveteau.datalog.core.interpreter

import io.github.alexandrepiveteau.datalog.core.Atom

/**
 * The context in which the evaluation is taking place.
 *
 * @param domain the set of atoms which form the domain of the constants in the program.
 */
data class Context(val domain: Sequence<Atom>)
