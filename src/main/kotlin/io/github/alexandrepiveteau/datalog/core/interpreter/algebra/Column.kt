package io.github.alexandrepiveteau.datalog.core.interpreter.algebra

import io.github.alexandrepiveteau.datalog.dsl.Value

/**
 * The kind of outputs which are possible for a [Relation] projection.
 *
 * @see Relation.project
 */
sealed interface Column<out T> {

  /** Projects a constant value for all rows. */
  data class Constant<out T>(val value: Value<T>) : Column<T>

  /** Projects a value from the row at the given index. */
  data class Index(val index: Int) : Column<Nothing>
}
