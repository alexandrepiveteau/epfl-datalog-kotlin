package io.github.alexandrepiveteau.datalog.core.interpreter

import io.github.alexandrepiveteau.datalog.core.Predicate

/** An interface representing an intentional database, with predicate derivation rules. */
internal interface IDB {

  /** Returns the arity of a predicate, or throws if not found. */
  fun arity(predicate: Predicate): Int

  /** Returns an [Iterator] with all predicates in the [IDB]. */
  operator fun iterator(): Iterator<Predicate>

  /** Returns the [Set] of [PredicateRule]s for the given [Predicate]. */
  operator fun get(predicate: Predicate): Set<PredicateRule>

  /** Returns a new [IDB] with the same contents as this [IDB] for the given predicates only. */
  fun filter(predicates: Collection<Predicate>): IDB

  /** Returns `true` if the [IDB] contains the given [Predicate], or `false` otherwise. */
  operator fun contains(predicate: Predicate): Boolean
}

/** An implementation of [IDB] backed by a [Map]. */
internal data class MapIDB(private val map: Map<Predicate, Set<PredicateRule>>) : IDB {
  override fun arity(predicate: Predicate) =
      map[predicate]?.firstOrNull()?.arity ?: error("Unknown predicate.")
  override fun iterator() = map.keys.iterator()
  override fun get(predicate: Predicate) = map[predicate] ?: emptySet()
  override fun filter(predicates: Collection<Predicate>) =
      MapIDB(map.filterKeys { it in predicates })
  override fun contains(predicate: Predicate) = predicate in map
}
