package io.github.alexandrepiveteau.datalog.dsl

import io.kotest.core.spec.style.StringSpec

class ArityTests : StringSpec() {
  init {

    "same predicate with different arity yields correct values" {
      program {
        val (a) = predicates()

        a(1, 1) += empty
        a(2, 2) += empty
        a(3) += empty

        expect(a, arity = 2) {
          add(listOf(1, 1))
          add(listOf(2, 2))
        }

        expect(a, arity = 1) { add(listOf(3)) }
      }
    }
  }
}
