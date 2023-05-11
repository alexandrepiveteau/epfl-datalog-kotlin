package io.github.alexandrepiveteau.datalog.core

/** An [Atom] is a constant or a variable. */
@JvmInline
value class Atom internal constructor(internal val backing: Int) {

  /** Returns true if this [Atom] is a constant. */
  val isConstant: Boolean
    get() = backing >= 0

  /** Returns true if this [Atom] is a variable. */
  val isVariable: Boolean
    get() = backing < 0

  override fun toString(): String {
    if (isConstant) return "c${backing}"
    return "v${-backing-1}"
  }
}
