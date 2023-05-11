package io.github.alexandrepiveteau.datalog.dsl

/** A [Relations] is a helper class which allows to create multiple [Relation]s at once. */
fun interface Relations<out T> {
  operator fun invoke(): Relation<T>
  operator fun component1(): Relation<T> = invoke()
  operator fun component2(): Relation<T> = invoke()
  operator fun component3(): Relation<T> = invoke()
  operator fun component4(): Relation<T> = invoke()
  operator fun component5(): Relation<T> = invoke()
  operator fun component6(): Relation<T> = invoke()
  operator fun component7(): Relation<T> = invoke()
  operator fun component8(): Relation<T> = invoke()
  operator fun component9(): Relation<T> = invoke()
}
