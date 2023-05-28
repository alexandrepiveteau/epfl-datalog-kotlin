package io.github.alexandrepiveteau.datalog.parser.core

/** A tuple with 2 elements. */
data class Tuple2<out A, out B>(val a: A, val b: B)

/** A tuple with 3 elements. */
data class Tuple3<out A, out B, out C>(val a: A, val b: B, val c: C)

/** A tuple with 4 elements. */
data class Tuple4<out A, out B, out C, out D>(val a: A, val b: B, val c: C, val d: D)
