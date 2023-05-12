package io.github.alexandrepiveteau.datalog.dsl

import kotlin.test.Test

class NegationTest {

  @Test
  fun `empty negated rule yields whole domain`() = program {
    constants(1, 2, 3)

    val (a, b) = predicates()
    val (x) = variables()

    b(x) += !a(x)

    expect(a) {}
    expect(b) {
      add(listOf(1))
      add(listOf(2))
      add(listOf(3))
    }
  }

  @Test
  fun `non-reachability in graph`() = program {
    val (e, tc, ntc) = predicates()
    val (x, y, z) = variables()

    e(1, 2) += empty
    e(2, 3) += empty

    tc(x, y) += e(x, y)
    tc(x, y) += tc(x, z) + e(z, y)
    ntc(x, y) += !tc(x, y)

    expect(tc) {
      add(listOf(1, 2))
      add(listOf(2, 3))
      add(listOf(1, 3))
    }

    expect(ntc) {
      add(listOf(1, 1))
      add(listOf(2, 1))
      add(listOf(2, 2))
      add(listOf(3, 1))
      add(listOf(3, 2))
      add(listOf(3, 3))
    }
  }
}
