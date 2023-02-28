package io.github.alexandrepiveteau.datalog.parser

import io.github.alexandrepiveteau.datalog.parser.core.*

val StatementParser =
    Symbol.LeftParenthesis.parser
        .flapMap { Parser.identifier() }
        .ignore(Symbol.Comma.parser)
        .andThen(Parser.identifier(), ::Statement)
        .ignore(Symbol.RightParenthesis.parser)
        .ignore(Symbol.Dot.parser)

data class Statement(val left: Text, val right: Text) : Node {
  override val parser: Parser<Text, Statement> =
      Symbol.LeftParenthesis.parser
          .flapMap { Parser.identifier() }
          .ignore(Symbol.Comma.parser)
          .andThen(Parser.identifier(), ::Statement)
          .ignore(Symbol.RightParenthesis.parser)
          .ignore(Symbol.Dot.parser)
}
