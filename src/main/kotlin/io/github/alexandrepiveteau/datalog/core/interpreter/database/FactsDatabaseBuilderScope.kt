package io.github.alexandrepiveteau.datalog.core.interpreter.database

import io.github.alexandrepiveteau.datalog.core.rule.Fact

// TODO : Document this.
internal fun interface FactsDatabaseBuilderScope<T> {
  fun add(predicate: PredicateWithArity, fact: Fact<T>)
}
