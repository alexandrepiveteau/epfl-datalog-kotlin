package io.github.alexandrepiveteau.datalog.dsl

import io.github.alexandrepiveteau.datalog.dsl.domains.IntDomain

/**
 * An interface representing information about the domain of constants in the program.
 *
 * @param T the type of the constants in the domain.
 */
interface Domain<T> {

  /** Returns a [T] representing the unit constant. */
  fun unit(): T

  /** Returns the sum of the two [T]s. */
  fun sum(a: T, b: T): T

  /** Returns the maximum value of the two [T]s. */
  fun max(a: T, b: T): T

  /** Returns the minimum value of the two [T]s. */
  fun min(a: T, b: T): T

  companion object {

    /** Returns a [Domain] for [Int] values. */
    fun int(): Domain<Int> = IntDomain
  }
}
