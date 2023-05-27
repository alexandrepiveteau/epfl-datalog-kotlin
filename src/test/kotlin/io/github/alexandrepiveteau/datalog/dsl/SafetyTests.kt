package io.github.alexandrepiveteau.datalog.dsl

import io.github.alexandrepiveteau.datalog.core.NotGroundedException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec

class SafetyTests : StringSpec() {
  init {

    "rule with no variables used is not safe" {
      shouldThrow<NotGroundedException> {
        program {
          val (a) = predicates()
          val (x) = variables()

          a(x) += empty
        }
      }
    }

    "rule with one variable not used is not safe" {
      shouldThrow<NotGroundedException> {
        program {
          val (a, b) = predicates()
          val (x, y) = variables()

          b(x, y) += a(y)
        }
      }
    }

    "rule with variable appearing only negative literal is not safe" {
      shouldThrow<NotGroundedException> {
        program {
          val (a, b) = predicates()
          val (x) = variables()

          a(x) += !b(x)
        }
      }
    }

    "aggregate of negative rule is not safe" {
      shouldThrow<NotGroundedException> {
        program {
          val (a, b) = predicates()
          val (x, s) = variables()

          a(s) += !b(x) + count(setOf(x), s)
        }
      }
    }
  }
}
