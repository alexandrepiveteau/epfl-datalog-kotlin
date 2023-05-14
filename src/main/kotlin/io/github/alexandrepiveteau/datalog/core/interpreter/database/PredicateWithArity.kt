package io.github.alexandrepiveteau.datalog.core.interpreter.database

import io.github.alexandrepiveteau.datalog.core.Predicate

/** A [Predicate] with an associated [arity]. */
internal data class PredicateWithArity(val predicate: Predicate, val arity: Int)
