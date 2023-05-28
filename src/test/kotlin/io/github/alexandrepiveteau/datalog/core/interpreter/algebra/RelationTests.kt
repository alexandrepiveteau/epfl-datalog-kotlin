package io.github.alexandrepiveteau.datalog.core.interpreter.algebra

import io.github.alexandrepiveteau.datalog.core.RuleBuilder
import io.github.alexandrepiveteau.datalog.dsl.domains.domain
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.ints.shouldBeZero

class RelationTests : StringSpec() {
  init {

    "aggregate on empty relation" {
      val relation = Relation.empty<Int>(2)
      val aggregate =
          relation.aggregate(
              projection = emptyList(),
              same = emptySet(),
              domain = Int.domain(),
              aggregate = RuleBuilder.AggregationFunction.Max,
              indices = emptySet(),
          )

      aggregate.arity.shouldBeZero()
      aggregate.tuples.shouldBeEmpty()
    }
  }
}
