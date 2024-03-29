package io.github.alexandrepiveteau.datalog.dsl

import io.github.alexandrepiveteau.datalog.core.NoStratificationException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec

class AggregateTests : StringSpec() {
  init {

    "cyclic aggregate" {
      shouldThrow<NoStratificationException> {
        program {
          val (p) = predicates()
          val (x, v, s) = variables()
          p(x, s) += p(x, v) + max(setOf(x), setOf(v), result = s)

          expect(p, 2) { /* Fails */}
        }
      }
    }

    "maximum from column subset" {
      program {
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
    }

    "minimum from column subset" {
      program {
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
    }

    "sum from column" {
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

    "count items without duplicates" {
      program {
        val (p, r) = predicates()
        val (v, s) = variables()

        p(1) += empty
        p(2) += empty
        p(3) += empty

        r(s) += p(v) + count(emptySet(), result = s)

        expect(r, arity = 1) { add(listOf(3)) }
      }
    }

    "count items with duplicates returns distinct count" {
      program {
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
    }

    "count items by type returns distinct count" {
      program {
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

    "negation on aggregation returns proper values" {
      program {
        val (p, q, r, c) = predicates()
        val (x, v, s) = variables()

        p(1, 1) += empty
        p(1, 2) += empty

        q(1) += empty
        q(2) += empty
        q(3) += empty
        q(4) += empty

        r(s) += p(v, x) + sum(setOf(v), setOf(x), result = s)
        c(x) += !r(x) + q(x)

        expect(c, arity = 1) {
          add(listOf(1))
          add(listOf(2))
          add(listOf(4))
        }
      }
    }

    "graph degree test" {
      program {
        val (edge, outDegree, inDegree) = predicates()
        val (x, y, c) = variables()

        edge(1, 2) += empty
        edge(1, 3) += empty
        edge(2, 3) += empty
        edge(3, 1) += empty
        edge(3, 4) += empty
        edge(3, 2) += empty

        outDegree(x, c) += edge(x, y) + count(setOf(x), c)
        inDegree(y, c) += edge(x, y) + count(setOf(y), c)

        expect(outDegree, 2) {
          add(listOf(1, 2))
          add(listOf(2, 1))
          add(listOf(3, 3))
        }

        expect(inDegree, 2) {
          add(listOf(1, 1))
          add(listOf(2, 2))
          add(listOf(3, 2))
          add(listOf(4, 1))
        }
      }
    }
  }
}
