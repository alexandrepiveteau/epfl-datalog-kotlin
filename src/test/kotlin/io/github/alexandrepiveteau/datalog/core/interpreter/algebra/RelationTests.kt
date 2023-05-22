package io.github.alexandrepiveteau.datalog.core.interpreter.algebra

import io.github.alexandrepiveteau.datalog.core.RuleBuilder
import io.github.alexandrepiveteau.datalog.core.interpreter.algebra.Column.Index
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RelationTests {

  @Test
  fun `aggregate on empty relation`() {
    val relation = Relation.empty(2)
    val aggregate =
        relation.aggregate(
            projection = emptyList(),
            same = emptySet(),
            domain = IntDomain,
            aggregate = RuleBuilder.Aggregate.Max,
            index = Index(0),
        )

    assertEquals(0, aggregate.arity)
    assertTrue(aggregate.tuples.isEmpty())
  }
}
