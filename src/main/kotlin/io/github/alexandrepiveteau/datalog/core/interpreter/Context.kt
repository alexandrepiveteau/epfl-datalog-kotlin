package io.github.alexandrepiveteau.datalog.core.interpreter

import io.github.alexandrepiveteau.datalog.core.Atom
import io.github.alexandrepiveteau.datalog.core.interpreter.algebra.Relation
import io.github.alexandrepiveteau.datalog.core.interpreter.algebra.domain
import io.github.alexandrepiveteau.datalog.core.interpreter.algebra.minus

/**
 * The context in which the evaluation is taking place.
 *
 * @param domain the set of atoms which form the domain of the constants in the program.
 */
internal data class Context(val domain: Sequence<Atom>) {

  /** Negates the [Relation] in this [Context]. */
  internal fun Relation.negated(): Relation = Relation.domain(arity, domain) - this
}
