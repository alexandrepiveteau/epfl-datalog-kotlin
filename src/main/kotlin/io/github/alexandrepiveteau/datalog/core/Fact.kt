package io.github.alexandrepiveteau.datalog.core

import io.github.alexandrepiveteau.datalog.dsl.Value

/**
 * A [Fact] is a [List] of constant [Value]s.
 *
 * @param T the type of the values.
 */
typealias Fact<T> = List<Value<T>>
