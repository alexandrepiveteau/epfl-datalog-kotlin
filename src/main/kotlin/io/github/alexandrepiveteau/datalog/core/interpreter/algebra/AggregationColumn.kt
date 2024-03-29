package io.github.alexandrepiveteau.datalog.core.interpreter.algebra

import io.github.alexandrepiveteau.datalog.core.interpreter.algebra.Column as CoreColumn

/**
 * The kind of outputs which are possible for a [Relation] aggregate projection.
 *
 * @see Relation.aggregate
 */
internal sealed interface AggregationColumn<out T> {

  /** Projects the aggregate value computed in the aggregation. */
  object Aggregate : AggregationColumn<Nothing>

  /** Projects a column for all rows. */
  data class Column<out T>(val column: CoreColumn<T>) : AggregationColumn<T>
}
