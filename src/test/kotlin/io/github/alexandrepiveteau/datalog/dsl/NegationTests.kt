package io.github.alexandrepiveteau.datalog.dsl

import io.github.alexandrepiveteau.datalog.core.NoStratificationException
import kotlin.test.Test
import kotlin.test.assertFailsWith

class NegationTests {

  @Test
  fun `program without stratification throws illegal state exception`() {
    assertFailsWith<NoStratificationException> {
      program {
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
    val (a, b, c, domain) = predicates()
    val (x) = variables()

    domain(1) += empty
    domain(2) += empty

    // This cannot be stratified.
    a(1) += empty
    a(x) += !a(x) + domain(x)

    // This can be stratified.
    b(2) += empty
    c(x) += !b(x) + domain(x)

    // We expect the program to terminate successfully.
    expect(c, arity = 1) { add(listOf(1)) }
  }

  @Test
  fun `empty negated rule yields whole domain`() = program {
    val (a, b, domain) = predicates()
    val (x) = variables()

    domain(1) += empty
    domain(2) += empty
    domain(3) += empty

    b(x) += !a(x) + domain(x)

    expect(a, arity = 1) {}
    expect(b, arity = 1) {
      add(listOf(1))
      add(listOf(2))
      add(listOf(3))
    }
  }

  @Test
  fun `non-reachability in graph`() = program {
    val (e, tc, ntc, v) = predicates()
    val (x, y, z) = variables()

    v(x) += e(x, y)
    v(y) += e(x, y)

    e(1, 2) += empty
    e(2, 3) += empty

    tc(x, y) += e(x, y)
    tc(x, y) += tc(x, z) + e(z, y)
    ntc(x, y) += !tc(x, y) + v(x) + v(y)

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

  @Test
  fun `transitive closure complement`() = program {
    val (r, v, t, tc) = predicates()
    val (x, y, z) = variables()

    // Set up the EDB
    r(1, 2) += empty
    r(2, 3) += empty
    r(3, 4) += empty
    r(4, 1) += empty

    // P1
    v(x) += r(x, y)
    v(y) += r(x, y)
    // P2
    t(x, y) += r(x, y)
    t(x, y) += t(x, z) + r(z, y)
    // P3
    tc(x, y) += v(x) + v(y) + !t(x, y)

    expect(tc, arity = 2) {} // Yields an empty set.
  }
}
