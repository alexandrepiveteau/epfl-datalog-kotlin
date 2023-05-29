package io.github.alexandrepiveteau.datalog.core.interpreter.ir

/** A key representing a database in which facts can be stored. */
@JvmInline
value class Database(private val backing: Any) {

  override fun toString(): String = backing.toString()

  companion object {

    /** A [Database] which is cleared on each query. */
    val Empty = Database("Empty")

    /** The [Database] where the base facts of the program are stored. */
    val Base = Database("Base")

    /** The [Database] where the result will be stored. */
    val Result = Database("Result")
  }
}
