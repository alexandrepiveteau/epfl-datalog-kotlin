package io.github.alexandrepiveteau.datalog.dsl

/** A [Predicates] is a helper class which allows to create multiple [Predicate]s at once. */
fun interface Predicates<out T> {
  operator fun invoke(): Predicate<T>
  operator fun component1(): Predicate<T> = invoke()
  operator fun component2(): Predicate<T> = invoke()
  operator fun component3(): Predicate<T> = invoke()
  operator fun component4(): Predicate<T> = invoke()
  operator fun component5(): Predicate<T> = invoke()
  operator fun component6(): Predicate<T> = invoke()
  operator fun component7(): Predicate<T> = invoke()
  operator fun component8(): Predicate<T> = invoke()
  operator fun component9(): Predicate<T> = invoke()
}
