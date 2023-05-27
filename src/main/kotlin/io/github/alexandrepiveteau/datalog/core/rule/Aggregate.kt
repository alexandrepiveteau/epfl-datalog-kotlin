package io.github.alexandrepiveteau.datalog.core.rule

import io.github.alexandrepiveteau.datalog.core.RuleBuilder

/**
 * An aggregate which matches a list of variables.
 *
 * @param aggregate the aggregation function to use.
 * @param same the collection of variables that must be equal.
 * @param columns the collection of columns to aggregate.
 * @param result the result of the aggregation.
 */
data class Aggregate(
    val aggregate: RuleBuilder.AggregationFunction,
    val same: Collection<Variable>,
    val columns: Collection<Variable>,
    val result: Variable,
)
