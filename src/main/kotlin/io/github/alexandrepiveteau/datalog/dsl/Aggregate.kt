package io.github.alexandrepiveteau.datalog.dsl

import io.github.alexandrepiveteau.datalog.core.rule.Variable
import io.github.alexandrepiveteau.datalog.core.RuleBuilder.Aggregate as CoreAggregate

/**
 * An aggregate which matches a list of variables.
 *
 * @param T the type of the elements in the relations.
 * @param aggregate the aggregation function to use.
 * @param same the collection of variables that must be equal.
 * @param columns the collection of columns to aggregate.
 * @param result the result of the aggregation.
 */
data class Aggregate<out T>(
    val aggregate: CoreAggregate,
    val same: Collection<Variable<T>>,
    val columns: Collection<Variable<T>>,
    val result: Variable<T>,
)
