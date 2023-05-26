package io.github.alexandrepiveteau.datalog.core.interpreter.database

import io.github.alexandrepiveteau.datalog.dsl.Value

// TODO : Document this.
internal fun interface FactsDatabaseBuilderScope<T> {
  fun add(predicate: PredicateWithArity, fact: List<Value<T>>)
}
