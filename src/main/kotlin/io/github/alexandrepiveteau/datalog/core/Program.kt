package io.github.alexandrepiveteau.datalog.core

import io.github.alexandrepiveteau.datalog.core.rule.Fact
import io.github.alexandrepiveteau.datalog.core.rule.Predicate

/**
 * A [Program] can be solved and generate some [Fact]s.
 *
 * @param T the type of the values in the [Fact]s.
 */
interface Program<out T> {

  /** Solves the given [Predicate] and returns an [Iterable] of [Fact]s that could be derived. */
  fun solve(predicate: Predicate, arity: Int): Iterable<Fact<T>>
}
