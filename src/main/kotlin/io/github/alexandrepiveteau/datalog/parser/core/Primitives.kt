package io.github.alexandrepiveteau.datalog.parser.core

// BUILDER PARSERS

/** A [Parser] for a [String] token. */
fun token(text: String): Parser<String> = Parser { state ->
  if (state.input.startsWith(text, state.from)) {
    Parser.Result.Success(text, state.copy(from = state.from + text.length))
  } else Parser.Result.Failure
}

/** A [Parser] for a [Regex] token. */
fun regexToken(text: Regex): Parser<String> = Parser { state ->
  when (val result = text.find(state.input, state.from)) {
    null -> Parser.Result.Failure
    else -> {
      val match = result.value
      Parser.Result.Success(match, state.copy(from = state.from + match.length))
    }
  }
}

/** A [Parser] that validates that the input is at the end of the string. */
fun end(): Parser<Unit> = Parser { state ->
  if (state.from == state.input.length) Parser.Result.Success(Unit, state)
  else Parser.Result.Failure
}

// DEFAULT TYPES PARSERS

/** A [Parser] for [Int] values. */
fun Int.Companion.parser(): Parser<Int> = regexToken(Regex("-?[0-9]+")).map { it.toInt() }
