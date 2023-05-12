package io.github.alexandrepiveteau.datalog.core.interpreter

import io.github.alexandrepiveteau.datalog.core.AtomList
import io.github.alexandrepiveteau.datalog.core.Predicate

/** A [PredicateRule] is a [Rule] for a given [Predicate]. */
internal data class PredicateRule(
    val predicate: Predicate,
    override val atoms: AtomList,
    override val clauses: List<PredicateClause>,
) : Rule
