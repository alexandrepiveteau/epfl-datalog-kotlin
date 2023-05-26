package io.github.alexandrepiveteau.datalog.core.interpreter.algebra

import io.github.alexandrepiveteau.datalog.core.RuleBuilder
import io.github.alexandrepiveteau.datalog.dsl.domains.domain
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RelationTests {

  @Test
  fun `aggregate on empty relation`() {
    val relation = Relation.empty<Int>(2)
    val aggregate =
        relation.aggregate(
            projection = emptyList(),
            same = emptySet(),
            domain = Int.domain(),
            aggregate = RuleBuilder.Aggregate.Max,
            indices = emptySet(),
        )

    assertEquals(0, aggregate.arity)
    assertTrue(aggregate.tuples.isEmpty())
  }
}
