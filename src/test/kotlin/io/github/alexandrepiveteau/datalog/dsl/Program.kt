package io.github.alexandrepiveteau.datalog.dsl

import kotlin.test.assertEquals
import kotlin.test.assertTrue

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
 * @param values a function that returns the expected values for the given [predicate].
 */
fun DatalogScope<Int>.expect(predicate: Predicate<Int>, values: MutableSet<List<Int>>.() -> Unit) {
  val expected = buildSet(values)
  val solution = solve(predicate)
  solution.forEach { assertEquals(predicate, it.predicate) } // all terms are in the right relation
  solution.forEach { kotlin.test.assertFalse(it.negated) } // no terms are negated
  solution.forEach { t -> assertTrue(t.atoms.none { it !is Value<Int> }) } // only values in result
  val res = solution.map { t -> t.atoms.mapNotNull { it as? Value<Int> }.map { it.value } }.toSet()
  assertEquals(expected, res)
}

/** A helper function that runs a datalog program with [Int]. */
fun program(scope: DatalogScope<Int>.() -> Unit): Unit = datalog { scope() }
