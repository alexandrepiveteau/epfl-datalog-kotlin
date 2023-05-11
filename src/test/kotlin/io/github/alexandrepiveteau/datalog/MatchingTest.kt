package io.github.alexandrepiveteau.datalog

import io.github.alexandrepiveteau.datalog.dsl.Relation
import io.github.alexandrepiveteau.datalog.dsl.Term
import io.github.alexandrepiveteau.datalog.dsl.Value
import io.github.alexandrepiveteau.datalog.dsl.Variable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/*class MatchingTest {

  @Test
  fun `no terms has empty matching`() {
    val terms = listOf<Term<Int>>()
    val matching = matching(*terms.toTypedArray())
    assertTrue(matching.isEmpty())
  }

  @Test
  fun `one constant term has empty matching`() {
    val relation = Relation<Int>(0)
    val terms = listOf(Term(relation, listOf(Value(1)), negated = false))
    val matching = matching(*terms.toTypedArray())
    assertTrue(matching.isEmpty())
  }

  @Test
  fun `many constant terms have empty matching`() {
    val relation = Relation<Int>(0)
    val terms =
        listOf(
            Term(relation, listOf(Value(1)), negated = false),
            Term(relation, listOf(Value(2)), negated = false),
            Term(relation, listOf(Value(3)), negated = false),
        )
    val matching = matching(*terms.toTypedArray())
    assertTrue(matching.isEmpty())
  }

  @Test
  fun `one term with one variable has non-empty matching`() {
    val relation = Relation<Int>(0)
    val variable = Variable<Int>(0)
    val terms = listOf(Term(relation, listOf(variable), negated = false))
    val matching = matching(*terms.toTypedArray())
    assertTrue(matching.isNotEmpty())
    assertEquals(listOf(setOf(0)), matching[variable])
  }

  @Test
  fun `one term with two variable occurrences has non-empty matching`() {
    val relation = Relation<Int>(0)
    val variable = Variable<Int>(0)
    val terms = listOf(Term(relation, listOf(variable, variable), negated = false))
    val matching = matching(*terms.toTypedArray())
    assertTrue(matching.isNotEmpty())
    assertEquals(listOf(setOf(0, 1)), matching[variable])
  }

  @Test
  fun `two terms with one shared variable has non-empty matching`() {
    val r1 = Relation<Int>(0)
    val r2 = Relation<Int>(0)
    val v1 = Variable<Int>(0)

    val terms =
        listOf(
            Term(r1, listOf(v1), negated = false),
            Term(r2, listOf(v1), negated = false),
        )
    val matching = matching(*terms.toTypedArray())

    assertTrue(matching.isNotEmpty())
    assertEquals(listOf(setOf(0), setOf(0)), matching[v1])
  }
}
*/