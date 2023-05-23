package io.github.alexandrepiveteau.datalog.core.interpreter.algebra

import io.github.alexandrepiveteau.datalog.core.Atom

/**
 * The kind of outputs which are possible for a [Relation] projection.
 *
 * @see Relation.project
 */
sealed interface Column {

  /** Projects a constant value for all rows. */
  data class Constant(val value: Atom) : Column

  /** Projects a value from the row at the given index. */
  data class Index(val index: Int) : Column
}
