package io.github.alexandrepiveteau.datalog.core.rule

/** A [Predicate] defines a set of rules. */
@JvmInline
value class Predicate internal constructor(private val backing: Any) {
  override fun toString(): String = "Relation(#${backing})"
}
