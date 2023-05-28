package io.github.alexandrepiveteau.datalog.parser

import io.github.alexandrepiveteau.datalog.core.RuleBuilder.AggregationFunction.Sum
import io.github.alexandrepiveteau.datalog.core.rule.*
import io.github.alexandrepiveteau.datalog.parser.core.*
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class ParserTests : StringSpec() {
  init {

    "parses a simple rule with one clause correctly" {
      val rule = "a(X):-b(X)."
      val expected =
          CombinationRule<Int>(
              head = HeadLiteral(Predicate("a"), listOf(Variable("X"))),
              body = listOf(BodyLiteral(Predicate("b"), listOf(Variable("X")), false)),
          )

      val parser = DatalogParser(Int.parser()) and -end()
      parser.parse(rule) shouldBe expected
    }

    "parses a rule with aggregation correctly" {
      val rule = "a(x,s):-b(x,v),sum((x),(v),s)."
      val expected =
          AggregationRule(
              head = HeadLiteral(Predicate("a"), listOf(Variable("x"), Variable("s"))),
              clause = BodyLiteral(Predicate("b"), listOf(Variable("x"), Variable("v")), false),
              aggregate =
                  Aggregate(
                      aggregate = Sum,
                      same = listOf(Variable("x")),
                      columns = listOf(Variable("v")),
                      result = Variable("s"),
                  ),
          )

      val parser = DatalogParser(Int.parser()) and -end()
      parser.parse(rule) shouldBe expected
    }
  }
}
