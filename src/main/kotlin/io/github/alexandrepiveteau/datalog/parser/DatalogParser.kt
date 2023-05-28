package io.github.alexandrepiveteau.datalog.parser

import io.github.alexandrepiveteau.datalog.core.RuleBuilder.AggregationFunction.*
import io.github.alexandrepiveteau.datalog.core.rule.*
import io.github.alexandrepiveteau.datalog.parser.core.*

/**
 * A [Parser] of [Rule]s.
 *
 * @param T the type of the constants.
 * @param constant the [Parser] for constants.
 */
class DatalogParser<out T>(constant: Parser<T>) : Parser<Rule<T>> {

  // Tokens and keywords.
  private val lpar = token("(")
  private val rpar = token(")")
  private val comma = token(",")
  private val dot = token(".")
  private val implies = token(":-")
  private val not = token("!")

  // Aggregate names.
  private val min = token("min") map { Min }
  private val max = token("max") map { Max }
  private val sum = token("sum") map { Sum }
  private val count = token("count") map { Count }

  // Names.
  private val ws = regexToken(Regex("\\s*"))
  private val name = -ws and regexToken(Regex("[a-zA-Z_][a-zA-Z0-9_]*")) and -ws

  // Datalog grammar.
  private val predicate = name map ::Predicate
  private val variable = name map ::Variable
  private val variables = separated(variable, comma)
  private val value = constant map ::Value
  private val atom = variable or value
  private val atoms = separated(atom, comma)
  private val literal = predicate and -lpar and atoms and -rpar
  private val headLiteral = literal map { (name, atoms) -> HeadLiteral(name, atoms) }
  private val bodyLiteral =
      (optional(not) map { it != null } and literal) map
          { (neg, lit) ->
            val (name, atoms) = lit
            BodyLiteral(name, atoms, neg)
          }
  private val bodyLiterals = separated(bodyLiteral, comma)
  private val aggregate =
      ((min or max or sum or count) and
          -lpar and
          -lpar and
          variables and // same
          -rpar and
          -comma and
          -lpar and
          variables and // columns
          -rpar and
          -comma and
          variable and // result
          -rpar) map { (agg, same, columns, result) -> Aggregate(agg, same, columns, result) }

  private val aggregationRule =
      (headLiteral and -implies and bodyLiteral and -comma and aggregate and -dot) map
          { (h, b, a) ->
            AggregationRule(h, b, a)
          }
  private val combinationRule =
      (headLiteral and -implies and bodyLiterals and -dot) map { (h, b) -> CombinationRule(h, b) }

  private val rule = aggregationRule or combinationRule

  override fun parse(state: Parser.State) = rule.parse(state)
}
