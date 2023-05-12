package io.github.alexandrepiveteau.datalog.core.interpreter

import io.github.alexandrepiveteau.datalog.core.AtomList
import io.github.alexandrepiveteau.datalog.core.Predicate

/** A [PredicateClause] is a [Clause] for a given [Predicate]. */
internal data class PredicateClause(
    val predicate: Predicate,
    override val atoms: AtomList,
    override val negated: Boolean,
) : Clause
