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
        p(x, s) += p(x, v) + max(listOf(x), v, result = s)

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

    p(x, s) += q(x, y, v) + max(listOf(x), v, result = s)
    r(y, s) += q(x, y, v) + max(listOf(y), v, result = s)

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

    p(x, s) += q(x, y, v) + min(listOf(x), v, result = s)
    r(y, s) += q(x, y, v) + min(listOf(y), v, result = s)

    expect(p, arity = 2) {
      add(listOf(1, 2))
      add(listOf(2, 4))
    }
    expect(r, arity = 2) {
      add(listOf(1, 2))
      add(listOf(2, 3))
    }
  }
}
