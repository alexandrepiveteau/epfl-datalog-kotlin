package io.github.alexandrepiveteau.datalog.parser.core

/** A [Parser] whose output is ignored. */
data class Skip<out O>(val parser: Parser<O>) : Parser<O> by parser

/** Skips a [Parser]. */
fun <O> skip(parser: Parser<O>): Skip<O> = Skip(parser)

/** @see skip */
operator fun <O> Parser<O>.unaryMinus(): Skip<O> = skip(this)

// `and` operator, with `Skip` support.

/** Applies this parser, and then applies [other] if this parser succeeds. */
@JvmName("andSkipTwo")
infix fun <A, B> Skip<A>.and(
    other: Parser<B>,
): Parser<B> = parser.and(other).map { (_, b) -> b }

/** Applies this parser, and then applies [other] if this parser succeeds. */
@JvmName("andSkipTwo")
infix fun <A, B> Parser<A>.and(
    other: Skip<B>,
): Parser<A> = and(other.parser).map { (a, _) -> a }

/** Applies this parser, and then applies [other] if this parser succeeds. */
@JvmName("andSkipThree")
infix fun <A, B, C> Skip<Tuple2<A, B>>.and(
    other: Parser<C>,
): Parser<C> = parser.and(other).map { (_, _, c) -> c }

/** Applies this parser, and then applies [other] if this parser succeeds. */
@JvmName("andSkipThree")
infix fun <A, B, C> Parser<Tuple2<A, B>>.and(
    other: Skip<C>,
): Parser<Tuple2<A, B>> = and(other.parser).map { (a, b) -> Tuple2(a, b) }

/** Applies this parser, and then applies [other] if this parser succeeds. */
@JvmName("andSkipFour")
infix fun <A, B, C, D> Skip<Tuple3<A, B, C>>.and(
    other: Parser<D>,
): Parser<D> = parser.and(other).map { (_, _, _, d) -> d }

/** Applies this parser, and then applies [other] if this parser succeeds. */
@JvmName("andSkipFour")
infix fun <A, B, C, D> Parser<Tuple3<A, B, C>>.and(
    other: Skip<D>,
): Parser<Tuple3<A, B, C>> = and(other.parser).map { (a, b, c) -> Tuple3(a, b, c) }
