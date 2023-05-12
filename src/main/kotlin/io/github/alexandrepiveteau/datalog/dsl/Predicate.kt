package io.github.alexandrepiveteau.datalog.dsl

import io.github.alexandrepiveteau.datalog.core.Predicate

/**
 * A class representing a relation of multiple terms.
 *
 * @param T the type of the elements in the relation.
 * @param id the unique identifier of the relation, which gives its identity to the relation.
 */
data class Predicate<out T>(val id: Predicate)
