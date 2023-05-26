package io.github.alexandrepiveteau.datalog.dsl.domains

import io.github.alexandrepiveteau.datalog.dsl.Domain
import io.github.alexandrepiveteau.datalog.dsl.Value

/** An implementation of [Domain] for [Int] values. */
internal object IntDomain : Domain<Int> {
  private val Unit = Value(1)
  override fun unit(): Value<Int> = Unit
  override fun sum(a: Value<Int>, b: Value<Int>): Value<Int> = Value(a.value + b.value)
  override fun max(a: Value<Int>, b: Value<Int>): Value<Int> = if (a.value > b.value) a else b
  override fun min(a: Value<Int>, b: Value<Int>): Value<Int> = if (a.value < b.value) a else b
}
