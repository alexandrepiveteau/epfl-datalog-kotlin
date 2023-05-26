package io.github.alexandrepiveteau.datalog.dsl

import io.github.alexandrepiveteau.datalog.core.rule.Value

/**
 * An interface representing information about the domain of constants in the program.
 *
 * @param T the type of the constants in the domain.
 */
interface Domain<T> {

  /** Returns a [T] representing the unit constant. */
  fun unit(): Value<T>

  /** Returns the sum of the two [T]s. */
  fun sum(a: Value<T>, b: Value<T>): Value<T>

  /** Returns the maximum value of the two [T]s. */
  fun max(a: Value<T>, b: Value<T>): Value<T>

  /** Returns the minimum value of the two [T]s. */
  fun min(a: Value<T>, b: Value<T>): Value<T>
}
