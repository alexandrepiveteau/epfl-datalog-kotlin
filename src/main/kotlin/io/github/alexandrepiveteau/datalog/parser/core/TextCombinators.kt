package io.github.alexandrepiveteau.datalog.parser.core

fun Parser.Companion.token(text: String): Parser<Text, Text> = token(Text.of(text))

fun Parser.Companion.token(text: Text): Parser<Text, Text> = Parser { input ->
  sequence { if (input.startsWith(text)) yield(Parser.Result(input.drop(text.length), text)) }
}

fun Parser.Companion.identifier(): Parser<Text, Text> = Parser { input ->
  sequence {
    val identifier = buildString {
      var index = 0
      while (index < input.length) {
        val c = input[index]
        if (c.isLetterOrDigit() || c == '_') {
          append(c)
          index++
        } else break
      }
    }
    if (identifier.isNotEmpty()) {
      yield(Parser.Result(input.drop(identifier.length), Text.of(identifier)))
    }
  }
}
