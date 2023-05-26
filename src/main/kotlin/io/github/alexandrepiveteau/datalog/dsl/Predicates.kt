package io.github.alexandrepiveteau.datalog.dsl

import io.github.alexandrepiveteau.datalog.core.rule.Predicate

/** A [Predicates] is a helper class which allows to create multiple [Predicate]s at once. */
fun interface Predicates {
  operator fun invoke(): Predicate
  operator fun component1(): Predicate = invoke()
  operator fun component2(): Predicate = invoke()
  operator fun component3(): Predicate = invoke()
  operator fun component4(): Predicate = invoke()
  operator fun component5(): Predicate = invoke()
  operator fun component6(): Predicate = invoke()
  operator fun component7(): Predicate = invoke()
  operator fun component8(): Predicate = invoke()
  operator fun component9(): Predicate = invoke()
}
