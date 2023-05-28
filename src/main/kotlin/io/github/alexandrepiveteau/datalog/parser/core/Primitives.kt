package io.github.alexandrepiveteau.datalog.parser.core

// BUILDER PARSERS

/** A [Parser] for a [String] token. */
fun token(text: String): Parser<String> = Parser { state ->
  if (state.input.startsWith(text, startIndex = state.from)) {
    Parser.Result.Success(text, state.copy(from = state.from + text.length))
  } else Parser.Result.Failure
}

/** A [Parser] for a [Regex] token. */
fun regexToken(text: Regex): Parser<String> = Parser { state ->
  when (val result = text.find(state.input, startIndex = state.from)) {
    null -> Parser.Result.Failure
    else -> {
      if (result.range.first != state.from) Parser.Result.Failure
      else Parser.Result.Success(result.value, state.copy(from = state.from + result.value.length))
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

/** A [Parser] for [String] values between quotes. */
fun String.Companion.parser(): Parser<String> =
    regexToken(Regex("\"[^\"]*\"")).map { it.drop(1).dropLast(1) }
