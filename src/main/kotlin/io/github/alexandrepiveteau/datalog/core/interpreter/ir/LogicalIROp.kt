package io.github.alexandrepiveteau.datalog.core.interpreter.ir

import io.github.alexandrepiveteau.datalog.core.RuleBuilder
import io.github.alexandrepiveteau.datalog.core.interpreter.algebra.*
import io.github.alexandrepiveteau.datalog.core.rule.Value

/**
 * An interface representing logical operations in the Datalog intermediate representation. The
 * relational algebra operations can be represented as a tree, where each node is an [LogicalIROp].
 *
 * @param T the type of the constants in the domain.
 */
internal sealed interface LogicalIROp<out T> {

  /** The arity of the relation produced by this operation. */
  val arity: Int

  /** Returns an empty relation with the given [arity]. */
  data class Empty(override val arity: Int) : LogicalIROp<Nothing>

  /** Returns a relation with the given [arity] and [values]. */
  data class Domain<out T>(
      override val arity: Int,
      val values: Collection<Value<T>>,
  ) : LogicalIROp<T>

  /** Scans the given [relation] and returns the result. */
  data class Scan<out T>(val relation: Relation<T>) : LogicalIROp<T> {
    override val arity = relation.arity
  }

  /** Selects the given [relation] with the given [selection]. */
  data class Select<out T>(
      val relation: LogicalIROp<T>,
      val selection: Set<Set<Column<T>>>,
  ) : LogicalIROp<T> by relation

  /** Joins the given [relations] together. */
  data class Join<out T>(val relations: List<LogicalIROp<T>>) : LogicalIROp<T> {
    override val arity = relations.sumOf { it.arity }
  }

  /** Unions the given relations together. */
  data class Union<out T>(
      val first: LogicalIROp<T>,
      val second: LogicalIROp<T>,
  ) : LogicalIROp<T> by first

  /** Removes duplicates from the given [relation]. */
  data class Distinct<out T>(val relation: LogicalIROp<T>) : LogicalIROp<T> by relation

  /** Returns the difference between the given [relation] and the [removed] relation. */
  data class Minus<out T>(
      val relation: LogicalIROp<T>,
      val removed: LogicalIROp<T>,
  ) : LogicalIROp<T> by relation

  /** Projects the given [relation] on the given [columns]. */
  data class Project<out T>(
      val relation: LogicalIROp<T>,
      val columns: List<Column<T>>,
  ) : LogicalIROp<T> {
    override val arity = columns.size
  }

  /** Aggregates the given [relation] with the given [projection]. */
  data class Aggregate<T>(
      val relation: LogicalIROp<T>,
      val projection: List<AggregationColumn<T>>,
      val same: Set<Column.Index>,
      val domain: io.github.alexandrepiveteau.datalog.core.Domain<T>,
      val aggregate: RuleBuilder.AggregationFunction,
      val indices: Set<Column.Index>,
  ) : LogicalIROp<T> {
    override val arity = projection.size
  }
}

/** Computes the [Relation] produced by an [LogicalIROp]. */
internal fun <T> LogicalIROp<T>.compute(): Relation<T> {
  return when (this) {
    is LogicalIROp.Aggregate ->
        relation.compute().aggregate(projection, same, domain, aggregate, indices)
    is LogicalIROp.Distinct -> relation.compute().distinct()
    is LogicalIROp.Domain -> Relation.domain(arity, values.asSequence())
    is LogicalIROp.Empty -> Relation.empty(arity)
    is LogicalIROp.Join -> relations.map(LogicalIROp<T>::compute).join()
    is LogicalIROp.Minus -> relation.compute().minus(removed.compute())
    is LogicalIROp.Project -> relation.compute().project(columns)
    is LogicalIROp.Scan -> relation
    is LogicalIROp.Select -> relation.compute().select(selection)
    is LogicalIROp.Union -> first.compute().union(second.compute())
  }
}
