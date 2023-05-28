package io.github.alexandrepiveteau.datalog.parser.core

import io.github.alexandrepiveteau.datalog.parser.core.Parser.Result.Failure
import io.github.alexandrepiveteau.datalog.parser.core.Parser.Result.Success

/** An interface representing a [Parser] that outputs a value of type [O]. */
fun interface Parser<out O> {

  /** The current [State] of the parser. */
  data class State(val input: String, val from: Int)

  /** The kinds of parsing result. */
  sealed interface Result<out O> {

    /** Indicates that [output] was parsed successfully. */
    data class Success<out O>(val output: O, val state: State) : Result<O>

    /** Indicates that the parser failed. */
    object Failure : Result<Nothing>
  }

  /** Parses the given [State]. */
  fun parse(state: State): Result<O>
}

/** Parses the given [String] */
fun <O : Any> Parser<O>.parse(input: String): O? {
  val state = Parser.State(input, from = 0)
  return when (val result = parse(state)) {
    Failure -> null
    is Success -> result.output
  }
}
