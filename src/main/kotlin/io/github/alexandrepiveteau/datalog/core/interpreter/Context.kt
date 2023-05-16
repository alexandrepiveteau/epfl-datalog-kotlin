package io.github.alexandrepiveteau.datalog.core.interpreter

import io.github.alexandrepiveteau.datalog.core.Atom
import io.github.alexandrepiveteau.datalog.core.Domain
import io.github.alexandrepiveteau.datalog.core.interpreter.algebra.Relation
import io.github.alexandrepiveteau.datalog.core.interpreter.algebra.domain
import io.github.alexandrepiveteau.datalog.core.interpreter.algebra.minus

/**
 * The context in which the evaluation is taking place.
 *
 * @param atoms the set of atoms which form the domain of the constants in the program.
 * @param domain the [Domain] on which the results are computed.
 */
internal data class Context(
    val atoms: Sequence<Atom>,
    val domain: Domain,
) {

  /** Negates the [Relation] in this [Context]. */
  internal fun Relation.negated(): Relation = Relation.domain(arity, atoms) - this
}
