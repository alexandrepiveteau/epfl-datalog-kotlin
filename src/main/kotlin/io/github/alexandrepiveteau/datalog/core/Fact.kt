package io.github.alexandrepiveteau.datalog.core

import io.github.alexandrepiveteau.datalog.dsl.Value

/**
 * A [Fact] defines some constant atoms for a [Predicate].
 *
 * @param T the type of the elements in the relation.
 */
data class Fact<out T>(val predicate: Predicate, val atoms: List<Value<T>>)
