package io.github.alexandrepiveteau.datalog.core.solver

import io.github.alexandrepiveteau.datalog.core.AtomList
import io.github.alexandrepiveteau.datalog.core.Relation

data class Rule(val relation: Relation, val atoms: AtomList, val clauses: List<Clause>)
