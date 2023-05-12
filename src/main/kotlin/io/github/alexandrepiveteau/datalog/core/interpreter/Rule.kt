package io.github.alexandrepiveteau.datalog.core.interpreter

import io.github.alexandrepiveteau.datalog.core.AtomList

/**
 * A [Rule] defines the derivation of a new relation, following the pattern defined by the
 * [AtomList] for production and using the [Clause]s for the derivation.
 */
internal interface Rule {

  /** The pattern that should be produced by the [Rule]. */
  val atoms: AtomList

  /** The [List] of [Clause]s that should be used to derive some new facts. */
  val clauses: List<Clause>

  /** Returns the [Int] arity of the rule. */
  val arity: Int
    get() = atoms.size
}
