package io.github.alexandrepiveteau.datalog.core

/** A [Relation] defines a set of rules. */
@JvmInline
value class Relation internal constructor(private val backing: Int) {
  override fun toString(): String = "Relation(#${backing})"
}
