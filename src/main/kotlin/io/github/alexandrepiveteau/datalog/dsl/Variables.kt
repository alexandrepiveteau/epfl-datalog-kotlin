package io.github.alexandrepiveteau.datalog.dsl

import io.github.alexandrepiveteau.datalog.core.rule.Variable

/** A [Variables] is a helper class which allows to create multiple [Variable]s at once. */
fun interface Variables {
  operator fun invoke(): Variable
  operator fun component1(): Variable = invoke()
  operator fun component2(): Variable = invoke()
  operator fun component3(): Variable = invoke()
  operator fun component4(): Variable = invoke()
  operator fun component5(): Variable = invoke()
  operator fun component6(): Variable = invoke()
  operator fun component7(): Variable = invoke()
  operator fun component8(): Variable = invoke()
  operator fun component9(): Variable = invoke()
}
