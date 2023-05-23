package io.github.alexandrepiveteau.datalog.dsl

import io.github.alexandrepiveteau.datalog.core.RuleBuilder.Aggregate as CoreAggregate

/**
 * An aggregate which matches a list of variables.
 *
 * @param T the type of the elements in the relations.
 * @param aggregate the aggregation function to use.
 * @param same the list of variables that must be equal.
 * @param columns the columns to aggregate.
 * @param result the result of the aggregation.
 */
data class Aggregate<out T>(
    val aggregate: CoreAggregate,
    val same: Iterable<Variable<T>>,
    val columns: Iterable<Variable<T>>,
    val result: Variable<T>,
)
