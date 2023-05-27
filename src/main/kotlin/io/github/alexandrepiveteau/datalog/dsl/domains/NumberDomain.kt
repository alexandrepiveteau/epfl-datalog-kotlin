package io.github.alexandrepiveteau.datalog.dsl.domains

import io.github.alexandrepiveteau.datalog.core.rule.Value
import io.github.alexandrepiveteau.datalog.core.Domain

/**
 * An implementation of [Domain] for [Number] values. This implementation is abstract, and is
 * intended to be used by the concrete implementations for [Int], [Long], [Float] and [Double].
 *
 * @param N the type of the number.
 * @param unit the unit value for the [Domain].
 * @param sum the function used to sum two [N] values.
 */
private abstract class NumberDomain<N>(
    unit: N,
    private val sum: (N, N) -> N,
) : Domain<N> where N : Number, N : Comparable<N> {
  private val one = Value(unit)
  override fun unit() = one
  override fun min(a: Value<N>, b: Value<N>) = if (a.value < b.value) a else b
  override fun max(a: Value<N>, b: Value<N>) = if (a.value > b.value) a else b
  override fun sum(a: Value<N>, b: Value<N>) = Value(sum(a.value, b.value))
}

/** An implementation of [Domain] for [Int] values. */
private object IntDomain : NumberDomain<Int>(1, Int::plus)

/** An implementation of [Domain] for [Long] values. */
private object LongDomain : NumberDomain<Long>(1L, Long::plus)

/** An implementation of [Domain] for [Float] values. */
private object FloatDomain : NumberDomain<Float>(1f, Float::plus)

/** An implementation of [Domain] for [Double] values. */
private object DoubleDomain : NumberDomain<Double>(1.0, Double::plus)

// Domains.

/** Returns the [Domain] for [Int] values. */
fun Int.Companion.domain(): Domain<Int> = IntDomain

/** Returns the [Domain] for [Long] values. */
fun Long.Companion.domain(): Domain<Long> = LongDomain

/** Returns the [Domain] for [Float] values. */
fun Float.Companion.domain(): Domain<Float> = FloatDomain

/** Returns the [Domain] for [Double] values. */
fun Double.Companion.domain(): Domain<Double> = DoubleDomain
