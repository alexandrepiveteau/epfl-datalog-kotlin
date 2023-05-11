package io.github.alexandrepiveteau.datalog.dsl

import kotlin.test.Test
import kotlin.test.assertEquals

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

  @Test
  fun `transitive closure complex`() {
    val solution = program {
      val (e, tc, r) = relations()
      val (x, y, z) = variables()

      e(1, 2) += empty
      e(2, 3) += empty
      e(3, 4) += empty
      e(4, 5) += empty
      e(5, 6) += empty
      e(6, 1) += empty

      tc(x, y) += e(x, y)
      tc(x, y) += tc(x, z) + e(z, y)

      r(x) += tc(1.asValue(), x)
      r
    }
    assertEquals(
        setOf(
            listOf(1),
            listOf(2),
            listOf(3),
            listOf(4),
            listOf(5),
            listOf(6),
        ),
        solution,
    )
  }
}
