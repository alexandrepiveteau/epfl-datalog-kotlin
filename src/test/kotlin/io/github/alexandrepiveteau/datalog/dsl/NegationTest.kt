package io.github.alexandrepiveteau.datalog.dsl

import kotlin.test.Test
import kotlin.test.assertEquals

class NegationTest {

  @Test
  fun `empty negated rule yields whole domain`() {
    val solution = program {
      constants(1, 2, 3)

      val (a, b) = relations()
      val (x) = variables()

      b(x) += !a(x)
      b
    }
    assertEquals(setOf(listOf(1), listOf(2), listOf(3)), solution)
  }

  @Test
  fun `non-reachability in graph`() {
    val solution = program {
      val (e, tc, ntc) = relations()
      val (x, y, z) = variables()

      e(1, 2) += empty
      e(2, 3) += empty

      tc(x, y) += e(x, y)
      tc(x, y) += tc(x, z) + e(z, y)
      ntc(x, y) += !tc(x, y)

      ntc
    }
    assertEquals(
        setOf(
            listOf(1, 1),
            listOf(2, 1),
            listOf(2, 2),
            listOf(3, 1),
            listOf(3, 2),
            listOf(3, 3),
        ),
        solution,
    )
  }
}
