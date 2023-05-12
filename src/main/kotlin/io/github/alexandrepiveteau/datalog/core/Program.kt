package io.github.alexandrepiveteau.datalog.core

/** A [Program] can be solved and generate some [Fact]s. */
interface Program {

  /** Solves the given [Predicate] and returns an [Iterable] of [Fact]s that could be derived. */
  fun solve(predicate: Predicate): Iterable<Fact>
}
