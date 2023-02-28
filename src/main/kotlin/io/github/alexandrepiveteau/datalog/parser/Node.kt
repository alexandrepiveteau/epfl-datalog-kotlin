package io.github.alexandrepiveteau.datalog.parser

import io.github.alexandrepiveteau.datalog.parser.core.Parser
import io.github.alexandrepiveteau.datalog.parser.core.Text

sealed interface Node {
  val parser: Parser<Text, Node>
}
