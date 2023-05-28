package io.github.alexandrepiveteau.datalog.core.interpreter

import io.github.alexandrepiveteau.datalog.core.Domain
import io.github.alexandrepiveteau.datalog.core.interpreter.ir.LogicalIROp
import io.github.alexandrepiveteau.datalog.core.interpreter.ir.LogicalIROp.*
import io.github.alexandrepiveteau.datalog.core.rule.Value

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

  /** Negates the [LogicalIROp] in this [Context]. */
  internal fun LogicalIROp<T>.negated(): LogicalIROp<T> = Minus(Domain(arity, atoms.toSet()), this)
}
