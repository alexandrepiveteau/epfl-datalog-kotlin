package io.github.alexandrepiveteau.datalog.dsl

import io.github.alexandrepiveteau.datalog.core.rule.BodyLiteral

/**
 * An [Aggregation] is the body of an aggregate rule. It contains a [Term] and an [Aggregate] to
 * apply on the term.
 *
 * @param T the type of the elements in the relation.
 * @param term the term to aggregate.
 * @param aggregate the aggregation function to apply.
 */
data class Aggregation<out T>(val term: BodyLiteral<T>, val aggregate: Aggregate)
