package io.github.alexandrepiveteau.datalog.core.interpreter

import io.github.alexandrepiveteau.datalog.core.interpreter.algebra.Relation
import io.github.alexandrepiveteau.datalog.core.interpreter.algebra.domain
import io.github.alexandrepiveteau.datalog.core.interpreter.algebra.minus
import io.github.alexandrepiveteau.datalog.core.rule.Value
import io.github.alexandrepiveteau.datalog.core.Domain

/**
 * The context in which the evaluation is taking place.
 *
 * @param atoms the set of atoms which form the domain of the constants in the program.
 * @param domain the [Domain] on which the results are computed.
 */
internal data class Context<T>(
    val atoms: Sequence<Value<T>>,
    val domain: Domain<T>,
) {

  /** Negates the [Relation] in this [Context]. */
  internal fun Relation<T>.negated(): Relation<T> = Relation.domain(arity, atoms) - this
}
