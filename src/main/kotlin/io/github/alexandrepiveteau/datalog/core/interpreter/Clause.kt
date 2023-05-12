package io.github.alexandrepiveteau.datalog.core.interpreter

import io.github.alexandrepiveteau.datalog.core.AtomList

/**
 * A [Clause] defines which parts of a relation should be matched when deriving some new facts. The
 * [atoms] define the pattern that should be matched, and the [negated] flag indicates whether the
 * pattern should be negated or not.
 */
internal interface Clause {

  /** The pattern that should be matched. */
  val atoms: AtomList

  /** True if the pattern should be negated, false otherwise. */
  val negated: Boolean

  /** Returns the arity of this [Clause]. */
  val arity: Int
    get() = atoms.size
}
