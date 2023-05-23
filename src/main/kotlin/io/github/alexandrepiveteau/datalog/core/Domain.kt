package io.github.alexandrepiveteau.datalog.core

/** An interface representing information about the domain of the constants in the program. */
interface Domain {

  /** Returns the sum of the two [Atom]s. */
  fun sum(a: Atom, b: Atom): Atom

  /** Returns the maximum of the two [Atom]s. */
  fun max(a: Atom, b: Atom): Atom

  /** Returns the minimum of the two [Atom]s. */
  fun min(a: Atom, b: Atom): Atom
}
