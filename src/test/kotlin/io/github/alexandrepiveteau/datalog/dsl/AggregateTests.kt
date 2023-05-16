package io.github.alexandrepiveteau.datalog.dsl

import kotlin.test.Ignore
import kotlin.test.Test

class AggregateTests {

  @Ignore
  @Test
  fun `maximum from column subset`() = program {
    val (p, q, r) = predicates()
    val (x, y, v, s) = variables()

    q(1, 1, 2) += empty
    q(1, 2, 3) += empty
    q(2, 1, 4) += empty

    p(x, y) += q(x, y, v) + max(listOf(x), v, s)
    r(x, y) += q(x, y, v) + max(listOf(y), v, s)

    expect(p, arity = 2) {
      add(listOf(1, 3))
      add(listOf(2, 4))
    }
    expect(r, arity = 2) {
      add(listOf(1, 4))
      add(listOf(2, 3))
    }
  }
}
