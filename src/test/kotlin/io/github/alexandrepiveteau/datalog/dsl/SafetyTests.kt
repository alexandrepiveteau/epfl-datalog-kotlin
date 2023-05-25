package io.github.alexandrepiveteau.datalog.dsl

import io.github.alexandrepiveteau.datalog.core.NotGroundedException
import kotlin.test.Test
import kotlin.test.assertFailsWith

class SafetyTests {

  @Test
  fun `rule with no variables used is not safe`() {
    assertFailsWith<NotGroundedException> {
      program {
        val (a) = predicates()
        val (x) = variables()

        a(x) += empty
      }
    }
  }

  @Test
  fun `rule with one variable not used is not safe`() {
    assertFailsWith<NotGroundedException> {
      program {
        val (a, b) = predicates()
        val (x, y) = variables()

        b(x, y) += a(y)
      }
    }
  }
}
