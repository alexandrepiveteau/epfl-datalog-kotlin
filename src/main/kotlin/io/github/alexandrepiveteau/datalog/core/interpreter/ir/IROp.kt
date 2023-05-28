package io.github.alexandrepiveteau.datalog.core.interpreter.ir

import io.github.alexandrepiveteau.datalog.core.RuleBuilder
import io.github.alexandrepiveteau.datalog.core.interpreter.algebra.*
import io.github.alexandrepiveteau.datalog.core.interpreter.database.PredicateWithArity
import io.github.alexandrepiveteau.datalog.core.interpreter.database.isNotEmpty
import io.github.alexandrepiveteau.datalog.core.interpreter.ir.IROp.*
import io.github.alexandrepiveteau.datalog.core.interpreter.ir.IROp.RelationalIROp.*
import io.github.alexandrepiveteau.datalog.core.rule.Value

/**
 * An interface representing logical operations in the Datalog intermediate representation. The
 * relational operations can be represented as a tree, where each node is an [IROp].
 *
 * @param T the type of the constants in the program.
 */
internal sealed interface IROp<out T> {

  /** Performs the given [operations] in order. */
  data class Sequence<out T>(val operations: List<IROp<T>>) : IROp<T>

  /** Stores the result of the given [relation] in [database] under the given [predicate]. */
  data class Store<out T>(
      val database: Database,
      val predicate: PredicateWithArity,
      val relation: RelationalIROp<T>,
  ) : IROp<T>

  /** Repeat the given [operation] until both databases are equal. */
  data class DoWhileNotEqual<out T>(
      val operation: IROp<T>,
      val first: Database,
      val second: Database,
  ) : IROp<T>

  /** Repeat the given [operation] while the database is not empty. */
  data class DoWhileNotEmpty<out T>(
      val operation: IROp<T>,
      val database: Database,
  ) : IROp<T>

  /** Merges the base and result database to base, and clears all others databases. */
  object MergeAndClear : IROp<Nothing>

  /** Relational algebra [IROp]s. */
  sealed interface RelationalIROp<out T> : IROp<Relation<T>> {

    /** The arity of the relation produced by this operation. */
    val arity: Int

    /** Returns an empty relation with the given [arity]. */
    data class Empty(override val arity: Int) : RelationalIROp<Nothing>

    /** Returns a relation with the given [arity] and [values]. */
    data class Domain<out T>(
        override val arity: Int,
        val values: Collection<Value<T>>,
    ) : RelationalIROp<T>

    /** Scans the given [predicate] and returns the result. */
    data class Scan<out T>(
        val database: Database,
        val predicate: PredicateWithArity,
    ) : RelationalIROp<T> {
      override val arity = predicate.arity
    }

    /** Selects the given [relation] with the given [selection]. */
    data class Select<out T>(
        val relation: RelationalIROp<T>,
        val selection: Set<Set<Column<T>>>,
    ) : RelationalIROp<T> by relation

    /** Joins the given [relations] together. */
    data class Join<out T>(val relations: List<RelationalIROp<T>>) : RelationalIROp<T> {
      override val arity = relations.sumOf { it.arity }
    }

    /** Unions the given relations together. */
    data class Union<out T>(
        val first: RelationalIROp<T>,
        val second: RelationalIROp<T>,
    ) : RelationalIROp<T> by first

    /** Removes duplicates from the given [relation]. */
    data class Distinct<out T>(val relation: RelationalIROp<T>) : RelationalIROp<T> by relation

    /** Returns the difference between the given [relation] and the [removed] relation. */
    data class Minus<out T>(
        val relation: RelationalIROp<T>,
        val removed: RelationalIROp<T>,
    ) : RelationalIROp<T> by relation

    /** Projects the given [relation] on the given [columns]. */
    data class Project<out T>(
        val relation: RelationalIROp<T>,
        val columns: List<Column<T>>,
    ) : RelationalIROp<T> {
      override val arity = columns.size
    }

    /** Aggregates the given [relation] with the given [projection]. */
    data class Aggregate<T>(
        val relation: RelationalIROp<T>,
        val projection: List<AggregationColumn<T>>,
        val same: Set<Column.Index>,
        val domain: io.github.alexandrepiveteau.datalog.core.Domain<T>,
        val aggregate: RuleBuilder.AggregationFunction,
        val indices: Set<Column.Index>,
    ) : RelationalIROp<T> {
      override val arity = projection.size
    }
  }
}

// INTERPRETATION OF THE IR

/** Executes the given [IROp] and returns the result. */
internal fun <T> IROp<T>.compute(storage: StorageManager<T>) {
  return when (this) {
    is Sequence -> operations.forEach { it.compute(storage) }
    is Store -> storage.database(database)[predicate] = relation.rel(storage)
    is DoWhileNotEqual -> {
      do operation.compute(storage) while (storage.database(first) != storage.database(second))
    }
    is DoWhileNotEmpty -> {
      do operation.compute(storage) while (storage.database(database).isNotEmpty())
    }
    is MergeAndClear -> {
      storage.database(Database.Base) += storage.database(Database.Result)
      storage.removeAll(keep = setOf(Database.Base))
    }
    is RelationalIROp<*> -> Unit
  }
}

/** Computes the [Relation] produced by an [RelationalIROp]. */
private fun <T> RelationalIROp<T>.rel(storage: StorageManager<T>): Relation<T> {
  return when (this) {
    is Aggregate -> relation.rel(storage).aggregate(projection, same, domain, aggregate, indices)
    is Distinct -> relation.rel(storage).distinct()
    is Domain -> Relation.domain(arity, values.asSequence())
    is Empty -> Relation.empty(arity)
    is Join -> relations.map { it.rel(storage) }.join()
    is Minus -> relation.rel(storage).minus(removed.rel(storage))
    is Project -> relation.rel(storage).project(columns)
    is Scan -> storage.database(database)[predicate]
    is Select -> relation.rel(storage).select(selection)
    is Union -> first.rel(storage).union(second.rel(storage))
  }
}
