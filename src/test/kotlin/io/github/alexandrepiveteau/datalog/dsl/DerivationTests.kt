package io.github.alexandrepiveteau.datalog.dsl

import kotlin.test.Test

class DerivationTests {

  @Test fun `no rules yields no facts`() = program { expect(predicate(), arity = 0) {} }

  @Test
  fun `no derivation rules yields original facts`() = program {
    val a = predicate()
    a(1) += empty
    a(2) += empty

    expect(a, arity = 1) {
      add(listOf(1))
      add(listOf(2))
    }
  }

  @Test
  fun `no derivation rules only yields facts from requested relation`() = program {
    val (a, b) = predicates()
    a(1) += empty
    b(2) += empty
    a(3) += empty
    b(4) += empty

    expect(a, arity = 1) {
      add(listOf(1))
      add(listOf(3))
    }

    expect(b, arity = 1) {
      add(listOf(2))
      add(listOf(4))
    }
  }

  @Test
  fun `simple derivation yields fact`() = program {
    val (a, b) = predicates()
    val (x) = variables()
    a(1) += empty
    b(x) += a(x)

    expect(b, arity = 1) { add(listOf(1)) }
  }

  @Test
  fun `simple derivation preserves intermediate result`() = program {
    val (a, b, c) = predicates()
    val (x) = variables()

    a(1) += empty
    b(x) += a(x)
    c(x) += b(x)

    expect(c, arity = 1) { add(listOf(1)) }
  }

  @Test
  fun `transitive closure`() = program {
    val (e, tc) = predicates()
    val (x, y, z) = variables()

    e(1, 2) += empty
    e(2, 3) += empty

    tc(x, y) += e(x, y)
    tc(x, y) += tc(x, z) + e(z, y)

    expect(tc, arity = 2) {
      add(listOf(1, 2))
      add(listOf(1, 3))
      add(listOf(2, 3))
    }
  }

  @Test
  fun `transitive closure complex`() = program {
    val (e, tc, r) = predicates()
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

    expect(r, arity = 1) {
      add(listOf(1))
      add(listOf(2))
      add(listOf(3))
      add(listOf(4))
      add(listOf(5))
      add(listOf(6))
    }
  }
}
