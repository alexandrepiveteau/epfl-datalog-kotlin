package io.github.alexandrepiveteau.datalog.core.rule

/** A [Literal] is a [Predicate] with some atoms. */
interface Literal<out T> {

  /** The [Predicate] of the [Literal]. */
  val predicate: Predicate

  /** The projection of the [Literal] as a [List] of [Atom]s. */
  val atoms: List<Atom<T>>

  /** Returns the [Int] arity of the [Literal]. */
  val arity: Int
    get() = atoms.size
}
