package io.github.alexandrepiveteau.datalog.dsl

import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * A helper function that runs a datalog program with the given [scope], and returns the set of
 * derived facts for the relation returned by the [scope].
 *
 * The program checks certain invariants:
 * - all the facts are in the right relation;
 * - no facts are negated; and
 * - no variables are in the result.
 *
 * @param scope a function that returns generates the program and returns the relation to solve.
 */
fun program(
    scope: DatalogScope<Int>.() -> Relation<Int>,
): Set<List<Int>> = datalog {
  val relation = scope()
  val solution = solve(relation)
  solution.forEach { assertEquals(relation, it.relation) } // all terms are in the right relation
  solution.forEach { kotlin.test.assertFalse(it.negated) } // no terms are negated
  solution.forEach { t -> assertTrue(t.atoms.none { it !is Value<Int> }) } // only values in result
  solution.map { t -> t.atoms.mapNotNull { it as? Value<Int> }.map { it.value } }.toSet()
}
