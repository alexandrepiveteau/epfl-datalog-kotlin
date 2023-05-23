package io.github.alexandrepiveteau.datalog.dsl.domains

import io.github.alexandrepiveteau.datalog.dsl.Domain

/** An implementation of [Domain] for [Int] values. */
internal object IntDomain : Domain<Int> {
  override fun sum(a: Int, b: Int): Int = a + b
  override fun max(a: Int, b: Int): Int = maxOf(a, b)
  override fun min(a: Int, b: Int): Int = minOf(a, b)
}
