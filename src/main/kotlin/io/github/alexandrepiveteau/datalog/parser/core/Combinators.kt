package io.github.alexandrepiveteau.datalog.parser.core

import io.github.alexandrepiveteau.datalog.parser.core.Parser.Result

/**
 * A [Parser] takes an input and attempts to return some [Result]s from it. Parsing is performed
 * lazily, and the results are produced in a [Sequence] which may be empty if the parsing fails.
 *
 * @param I the type of the input.
 * @param O the type of the output.
 */
fun interface Parser<I, out O> {

  /**
   * Parses the given [input] and returns a [Sequence] of [Result]s.
   *
   * @param input the input to parse.
   * @return the [Sequence] of [Result]s.
   */
  fun parse(input: I): Sequence<Result<I, O>>

  /**
   * A [Result] is a pair of an [value] and a [remaining] input.
   *
   * @param I the type of the input.
   * @param O the type of the output.
   */
  data class Result<out I, out O>(val remaining: I, val value: O)

  companion object // Used to add parser factories.
}

/**
 * Returns a parser which always fails.
 *
 * @param I the type of the input.
 */
fun <I> Parser.Companion.failure(): Parser<I, Nothing> = Parser { emptySequence() }

/**
 * Returns a parser which always succeeds and does not chomp any input.
 *
 * @param result the value to return.
 *
 * @param I the type of the input.
 * @param O the type of the output.
 */
fun <I, O> Parser.Companion.success(
    result: O,
): Parser<I, O> = Parser { sequenceOf(Result(it, result)) }

/**
 * Returns a parser which returns the union of multiple parsers.
 *
 * @param parsers the parsers to union.
 *
 * @param I the type of the input.
 * @param O the type of the output.
 */
fun <I, O> combine(
  vararg parsers: Parser<I, O>,
): Parser<I, O> = Parser { input -> parsers.asSequence().flatMap { it.parse(input) } }

/**
 * Returns a parser which returns the union of this parser and an [other] parser.
 *
 * @param other the other parser to union.
 *
 * @param I the type of the input.
 * @param O the type of the output.
 * @see combine the equivalent function for multiple parsers.
 */
fun <I, O> Parser<I, O>.or(
  other: Parser<I, O>,
): Parser<I, O> = combine(this, other)

/**
 * Returns a parser whose results are transformed by the given function.
 *
 * @param f the function to apply to the results.
 *
 * @param I the type of the input.
 * @param O1 the type of the original output.
 * @param O2 the type of the transformed output.
 */
inline fun <I, O1, O2> Parser<I, O1>.map(
    crossinline f: (O1) -> O2,
): Parser<I, O2> = Parser { parse(it).map { (r, v) -> Result(r, f(v)) } }

/**
 * Returns a parser whose results are transformed by the given function.
 *
 * @param f the function to apply to the results.
 *
 * @param I the type of the input.
 * @param O1 the type of the original output.
 * @param O2 the type of the transformed output.
 */
inline fun <I, O1, O2> Parser<I, O1>.flapMap(
  crossinline f: (O1) -> Parser<I, O2>,
): Parser<I, O2> = Parser { parse(it).flatMap { (r, v) -> f(v).parse(r) } }

fun <I, O1, O2> Parser<I, O1>.andThen(
  parser: Parser<I, O1>,
  combine: (O1, O1) -> O2,
): Parser<I, O2> = flapMap { a -> parser.map { b -> combine(a, b) } }

/**
 * Returns a parser which returns the results of this parser, but only if the second parser is
 * successful as well.
 *
 * @param parser the other parser to apply.
 *
 * @param I the type of the input.
 * @param O the type of the output.
 */
fun <I, O> Parser<I, O>.ignore(parser: Parser<I, *>): Parser<I, O> = Parser { input ->
  parse(input).flatMap { (r, v) -> parser.parse(r).map { Result(it.remaining, v) } }
}

/**
 * Returns a parser which returns the results of this parser, but only if the predicate is valid.
 *
 * @param keepIf the predicate to apply to the results.
 *
 * @param I the type of the input.
 * @param O the type of the output.
 */
fun <I, O> Parser<I, O>.filter(keepIf: (O) -> Boolean): Parser<I, O> = Parser { input ->
  parse(input).filter { keepIf(it.value) }
}
