package io.github.alexandrepiveteau.datalog.core.rule

/** A [Rule] represents some kind of derivation used to derive new facts from existing ones. */
sealed interface Rule<out T> {

  /** The [HeadLiteral] of the [Rule]. */
  val head: HeadLiteral<T>

  /** The [BodyLiteral]s of the [Rule]. */
  val body: List<BodyLiteral<T>>
}
