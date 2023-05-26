package io.github.alexandrepiveteau.datalog.dsl

import io.github.alexandrepiveteau.datalog.core.NoStratificationException
import kotlin.test.Test
import kotlin.test.assertFailsWith

class AggregateTests {

  @Test
  fun `cyclic aggregate`() {
    assertFailsWith<NoStratificationException> {
      program {
        val (p) = predicates()
        val (x, v, s) = variables()
        p(x, s) += p(x, v) + max(setOf(x), setOf(v), result = s)

        expect(p, 2) { /* Fails */}
      }
    }
  }

  @Test
  fun `maximum from column subset`() = program {
    val (p, q, r) = predicates()
    val (x, y, v, s) = variables()

    q(1, 1, 2) += empty
    q(1, 2, 3) += empty
    q(2, 1, 4) += empty

    p(x, s) += q(x, y, v) + max(setOf(x), setOf(v), result = s)
    r(y, s) += q(x, y, v) + max(setOf(y), setOf(v), result = s)

    expect(p, arity = 2) {
      add(listOf(1, 3))
      add(listOf(2, 4))
    }
    expect(r, arity = 2) {
      add(listOf(1, 4))
      add(listOf(2, 3))
    }
  }

  @Test
  fun `minimum from column subset`() = program {
    val (p, q, r) = predicates()
    val (x, y, v, s) = variables()

    q(1, 1, 2) += empty
    q(1, 2, 3) += empty
    q(2, 1, 4) += empty

    p(x, s) += q(x, y, v) + min(setOf(x), setOf(v), result = s)
    r(y, s) += q(x, y, v) + min(setOf(y), setOf(v), result = s)

    expect(p, arity = 2) {
      add(listOf(1, 2))
      add(listOf(2, 4))
    }
    expect(r, arity = 2) {
      add(listOf(1, 2))
      add(listOf(2, 3))
    }
  }

  @Test
  fun `sum from column`() = program {
    val (p, r) = predicates()
    val (v, s) = variables()

    p(1) += empty
    p(2) += empty
    p(3) += empty

    r(s) += p(v) + sum(emptySet(), setOf(v), result = s)

    expect(r, arity = 1) { add(listOf(6)) }
  }

  @Test
  fun `count items without duplicates`() = program {
    val (p, r) = predicates()
    val (v, s) = variables()

    p(1) += empty
    p(2) += empty
    p(3) += empty

    r(s) += p(v) + count(emptySet(), result = s)

    expect(r, arity = 1) { add(listOf(3)) }
  }

  @Test
  fun `count items with duplicates returns distinct count`() = program {
    val (p, r) = predicates()
    val (v, s) = variables()

    p(1) += empty
    p(2) += empty
    p(2) += empty
    p(3) += empty
    p(3) += empty
    p(3) += empty

    r(s) += p(v) + count(emptySet(), result = s)

    expect(r, arity = 1) { add(listOf(3)) }
  }

  @Test
  fun `count items by type returns distinct count`() = program {
    val (p, r) = predicates()
    val (v, t) = variables()

    p(1, 1) += empty
    p(2, 1) += empty
    p(2, 2) += empty
    p(3, 1) += empty
    p(3, 2) += empty
    p(3, 3) += empty
    p(3, 4) += empty

    r(t, v) += p(t, v) + count(setOf(t), result = v)

    expect(r, arity = 2) {
      add(listOf(1, 1))
      add(listOf(2, 2))
      add(listOf(3, 4))
    }
  }
}
