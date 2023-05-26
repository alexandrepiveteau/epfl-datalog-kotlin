package io.github.alexandrepiveteau.datalog.dsl

import io.github.alexandrepiveteau.datalog.core.rule.Variable

/** A [Variables] is a helper class which allows to create multiple [Variable]s at once. */
fun interface Variables<out T> {
  operator fun invoke(): Variable<T>
  operator fun component1(): Variable<T> = invoke()
  operator fun component2(): Variable<T> = invoke()
  operator fun component3(): Variable<T> = invoke()
  operator fun component4(): Variable<T> = invoke()
  operator fun component5(): Variable<T> = invoke()
  operator fun component6(): Variable<T> = invoke()
  operator fun component7(): Variable<T> = invoke()
  operator fun component8(): Variable<T> = invoke()
  operator fun component9(): Variable<T> = invoke()
}
