package io.github.alexandrepiveteau.datalog.dsl

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DslTest {

  @Test
  fun `no rules yields no facts`() {
    val solution = program { relation() }
    assertEquals(emptySet(), solution)
  }

  @Test
  fun `no derivation rules yields original facts`() {
    val solution = program {
      val a = relation()
      a(1) += empty
      a(2) += empty
      a
    }
    assertEquals(setOf(listOf(1), listOf(2)), solution)
  }

  @Test
  fun `no derivation rules only yields facts from requested relation`() {
    val solution = program {
      val (a, b) = relations()
      a(1) += empty
      b(2) += empty
      a(3) += empty
      b(4) += empty
      a
    }
    assertEquals(setOf(listOf(1), listOf(3)), solution)
  }

  @Test
  fun `simple derivation yields fact`() {
    val solution = program {
      val (a, b) = relations()
      val (x) = variables()
      a(1) += empty
      b(x) += a(x)
      b
    }
    assertEquals(setOf(listOf(1)), solution)
  }

  @Test
  fun `transitive closure`() {
    val solution = program {
      val (e, tc) = relations()
      val (x, y, z) = variables()

      e(1, 2) += empty
      e(2, 3) += empty

      tc(x, y) += e(x, y)
      tc(x, y) += tc(x, z) + e(z, y)
      tc
    }
    assertEquals(setOf(listOf(1, 2), listOf(1, 3), listOf(2, 3)), solution)
  }
}

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
private fun program(
    scope: DatalogScope<Int>.() -> Relation<Int>,
): Set<List<Int>> = datalog {
  val relation = scope()
  val solution = solve(relation)
  solution.forEach { assertEquals(relation, it.relation) } // all terms are in the right relation
  solution.forEach { kotlin.test.assertFalse(it.negated) } // no terms are negated
  solution.forEach { t -> assertTrue(t.atoms.none { it !is Value<Int> }) } // only values in result
  solution.map { t -> t.atoms.mapNotNull { it as? Value<Int> }.map { it.value } }.toSet()
}
