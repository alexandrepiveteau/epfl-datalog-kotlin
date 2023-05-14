package io.github.alexandrepiveteau.datalog.core.interpreter.database

import io.github.alexandrepiveteau.datalog.core.AtomList

// TODO : Document this.
internal fun interface FactsDatabaseBuilderScope {
  fun add(predicate: PredicateWithArity, fact: AtomList)
}
