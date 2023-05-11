package io.github.alexandrepiveteau.datalog.core.solver

import io.github.alexandrepiveteau.datalog.core.AtomList
import io.github.alexandrepiveteau.datalog.core.Relation

data class Clause(val relation: Relation, val atoms: AtomList, val negated: Boolean)
