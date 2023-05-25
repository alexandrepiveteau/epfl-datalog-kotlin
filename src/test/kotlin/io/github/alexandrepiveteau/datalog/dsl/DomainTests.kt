package io.github.alexandrepiveteau.datalog.dsl

import io.github.alexandrepiveteau.datalog.core.NoSuchAtomException
import kotlin.test.Test
import kotlin.test.assertFailsWith

class DomainTests {

  @Test
  fun `sum from column requires constants to be defined`() {
    assertFailsWith<NoSuchAtomException> {
      program {
        val (p, r) = predicates()
        val (v, s) = variables()

        p(1) += empty
        p(2) += empty
        p(3) += empty

        r(s) += p(v) + sum(emptySet(), setOf(v), result = s)

        expect(r, arity = 1) { add(listOf(6)) }
      }
    }
  }
}
