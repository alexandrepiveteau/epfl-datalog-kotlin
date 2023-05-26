package io.github.alexandrepiveteau.datalog.dsl

import io.github.alexandrepiveteau.datalog.core.Algorithm
import io.github.alexandrepiveteau.datalog.core.Predicate
import kotlin.test.assertEquals

/**
 * A helper function that checks that the given [predicate] has values that match the values
 * produced in the [values] function.
 *
 * The program checks certain invariants:
 * - all the facts are in the right relation;
 * - no facts are negated; and
 * - no variables are in the result.
 *
 * @param predicate the [Predicate] to solve.
 * @param arity the arity of the [Predicate] to solve.
 * @param values a function that returns the expected values for the given [predicate].
 */
fun DatalogScope<Int>.expect(
    predicate: Predicate,
    arity: Int,
    values: MutableSet<List<Int>>.() -> Unit
) {
  val expected = buildSet(values)
  val solution = solve(predicate, arity)
  solution.forEach { assertEquals(arity, it.size) } // all terms have the right arity
  val res = solution.map { it.map { (v) -> v } }.toSet()
  assertEquals(expected, res)
}

/**
 * A helper function that runs a datalog program with [Int], testing the program with multiple
 * configurations.
 *
 * @param scope the scope in which the Datalog program is run.
 */
fun program(scope: DatalogScope<Int>.() -> Unit) {
  for (algorithm in Algorithm.values()) {
    datalog(Int.domain(), algorithm, scope)
  }
}
