package io.github.alexandrepiveteau.datalog.dsl

import io.github.alexandrepiveteau.datalog.core.RuleBuilder.Aggregate as CoreAggregate

/**
 * An aggregate which matches a list of variables.
 *
 * @param T the type of the elements in the relations.
 * @param aggregate the aggregation function to use.
 * @param same the list of variables that must be equal.
 * @param column the column to aggregate.
 * @param result the result of the aggregation.
 */
data class Aggregate<out T>(
    val aggregate: CoreAggregate,
    val same: List<Variable<T>>,
    val column: Variable<T>,
    val result: Variable<T>,
)
