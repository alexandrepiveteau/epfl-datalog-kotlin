package io.github.alexandrepiveteau.datalog.parser

import io.github.alexandrepiveteau.datalog.parser.core.Parser
import io.github.alexandrepiveteau.datalog.parser.core.Text
import io.github.alexandrepiveteau.datalog.parser.core.map
import io.github.alexandrepiveteau.datalog.parser.core.token

enum class Symbol(value: String) : Node {
  LeftParenthesis("("),
  RightParenthesis(")"),
  Comma(","),
  Dot(".");
  override val parser: Parser<Text, Symbol> = Parser.token(value).map { this }
}
