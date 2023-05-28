package io.github.alexandrepiveteau.datalog.parser.core

/** Maps the value produced by the current parser. */
inline infix fun <A, B> Parser<A>.map(crossinline f: (A) -> B): Parser<B> = Parser {
  when (val result = parse(it)) {
    Parser.Result.Failure -> Parser.Result.Failure
    is Parser.Result.Success -> {
      val (a, s) = result
      Parser.Result.Success(f(a), s)
    }
  }
}

/** Flat maps the value produced by the current parser. */
inline infix fun <A, B> Parser<A>.flatMap(crossinline f: (A) -> Parser<B>): Parser<B> = Parser {
  when (val result = parse(it)) {
    Parser.Result.Failure -> Parser.Result.Failure
    is Parser.Result.Success -> {
      val (a, s) = result
      f(a).parse(s)
    }
  }
}

/** Attempts to apply the current parser, or tries to apply [other] if the current parser fails. */
infix fun <A> Parser<A>.or(other: Parser<A>) = Parser { state ->
  when (val result = parse(state)) {
    Parser.Result.Failure -> other.parse(state)
    is Parser.Result.Success -> result
  }
}

/** Applies this parser, and then applies [other] if this parser succeeds. */
@JvmName("andTwo")
infix fun <A, B> Parser<A>.and(
    other: Parser<B>,
): Parser<Tuple2<A, B>> = flatMap { a -> other.map { b -> Tuple2(a, b) } }

/** Applies this parser, and then applies [other] if this parser succeeds. */
@JvmName("andThree")
infix fun <A, B, C> Parser<Tuple2<A, B>>.and(
    other: Parser<C>,
): Parser<Tuple3<A, B, C>> = flatMap { (a, b) -> other.map { c -> Tuple3(a, b, c) } }

/** Applies this parser, and then applies [other] if this parser succeeds. */
@JvmName("andFour")
infix fun <A, B, C, D> Parser<Tuple3<A, B, C>>.and(
    other: Parser<D>,
): Parser<Tuple4<A, B, C, D>> = flatMap { (a, b, c) -> other.map { d -> Tuple4(a, b, c, d) } }

/** Attempts to apply the given parser, or returns `null` if the parser fails. */
fun <A : Any> optional(parser: Parser<A>): Parser<A?> = Parser { state ->
  when (val result = parser.parse(state)) {
    Parser.Result.Failure -> Parser.Result.Success(null, state)
    is Parser.Result.Success -> result
  }
}

/** Attempts to apply the given parser, interspersed with the given separator. */
fun <A, B> separated(parser: Parser<A>, separator: Parser<B>): Parser<List<A>> = Parser { state ->
  val list = mutableListOf<A>()
  var currentState = state
  loop@ while (true) {
    when (val result = parser.parse(currentState)) {
      Parser.Result.Failure -> break@loop
      is Parser.Result.Success -> {
        val (a, s) = result
        list.add(a)
        currentState = s
      }
    }
    when (val result = separator.parse(currentState)) {
      Parser.Result.Failure -> break@loop
      is Parser.Result.Success -> {
        val (_, s) = result
        currentState = s
      }
    }
  }
  Parser.Result.Success(list, currentState)
}
