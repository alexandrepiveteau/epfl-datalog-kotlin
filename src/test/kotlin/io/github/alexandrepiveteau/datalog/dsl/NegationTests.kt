package io.github.alexandrepiveteau.datalog.dsl

import kotlin.test.Test
import kotlin.test.assertFailsWith

class NegationTests {

  @Test
  fun `program without stratification throws illegal state exception`() {
    assertFailsWith<IllegalStateException> {
      program {
        constants(1, 2)
        val (a) = predicates()
        val (x) = variables()

        a(1) += empty
        a(x) += !a(x)

        // This would loop forever if we didn't enforce stratification.
        expect(a, arity = 1) {}
      }
    }
  }

  @Test
  fun `program without stratification does not throw if predicate computable`() = program {
    constants(1, 2)
    val (a, b, c) = predicates()
    val (x) = variables()

    // This cannot be stratified.
    a(1) += empty
    a(x) += !a(x)

    // This can be stratified.
    b(2) += empty
    c(x) += !b(x)

    // We expect the program to terminate successfully.
    expect(c, arity = 1) { add(listOf(1)) }
  }

  @Test
  fun `empty negated rule yields whole domain`() = program {
    constants(1, 2, 3)

    val (a, b) = predicates()
    val (x) = variables()

    b(x) += !a(x)

    expect(a, arity = 1) {}
    expect(b, arity = 1) {
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

    expect(tc, arity = 2) {
      add(listOf(1, 2))
      add(listOf(2, 3))
      add(listOf(1, 3))
    }

    expect(ntc, arity = 2) {
      add(listOf(1, 1))
      add(listOf(2, 1))
      add(listOf(2, 2))
      add(listOf(3, 1))
      add(listOf(3, 2))
      add(listOf(3, 3))
    }
  }
}
