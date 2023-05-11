package io.github.alexandrepiveteau.datalog.dsl

/**
 * A class representing an unordered set of [Term]s. This is useful when we want to create a new
 * rule with multiple terms on the right-hand side.
 *
 * @param T the type of the elements in the relation.
 * @param terms the terms in this set.
 */
data class Terms<out T>(val terms: Set<Term<T>>)
