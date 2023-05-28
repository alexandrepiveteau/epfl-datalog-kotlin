package io.github.alexandrepiveteau.datalog.core.interpreter.ir

import io.github.alexandrepiveteau.datalog.core.interpreter.database.MutableFactsDatabase
import io.github.alexandrepiveteau.datalog.core.interpreter.database.empty

/**
 * The [StorageManager] represents the storage of the base and derived facts of the program, and
 * acts as the runtime representation of the [Database]s.
 */
internal class StorageManager<T> {

  /** The [MutableFactsDatabase]s that are currently stored in the [StorageManager]. */
  private val databases = mutableMapOf<Database, MutableFactsDatabase<T>>()

  /**
   * Retrieves a [MutableFactsDatabase] for the given [database], or creates a new one if it does
   * not exist, and stores it.
   *
   * @param database the [Database] for which the [MutableFactsDatabase] should be retrieved.
   */
  fun database(database: Database): MutableFactsDatabase<T> {
    if (database == Database.Empty) return MutableFactsDatabase.empty()
    return databases.getOrPut(database) { MutableFactsDatabase.empty() }
  }

  /** Removes all [Database]s, but keeps the specified entries. */
  fun removeAll(keep: Set<Database> = emptySet()) {
    val toRemove = databases.keys - keep
    for (database in toRemove) databases.remove(database)
  }
}
