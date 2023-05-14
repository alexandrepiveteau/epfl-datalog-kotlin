package io.github.alexandrepiveteau.datalog.core.interpreter.database

import io.github.alexandrepiveteau.datalog.core.interpreter.PredicateRule

/**
 * An intentional database, with predicate derivation rules. Each set of rules is associated with a
 * predicate and an arity. The same predicate can be associated with a different set of rules and
 * may have different arity.
 */
internal interface RulesDatabase {

  /** Returns an [Iterator] with all keys in the [RulesDatabase]. */
  operator fun iterator(): Iterator<PredicateWithArity>

  /** Returns the [Set] of [PredicateRule]s for the given [PredicateWithArity]. */
  operator fun get(key: PredicateWithArity): Set<PredicateRule>

  /**
   * Returns a new [RulesDatabase] with the same contents as this [RulesDatabase] for the given keys
   * only.
   */
  fun filter(keys: Collection<PredicateWithArity>): RulesDatabase

  /**
   * Returns `true` if the [RulesDatabase] contains the given [PredicateWithArity], or `false`
   * otherwise.
   */
  operator fun contains(key: PredicateWithArity): Boolean
}

internal data class MapRulesDatabase(
    private val map: Map<PredicateWithArity, Set<PredicateRule>>,
) : RulesDatabase {

  override fun iterator(): Iterator<PredicateWithArity> = map.keys.iterator()

  override fun get(key: PredicateWithArity): Set<PredicateRule> = map[key] ?: emptySet()

  override fun filter(
      keys: Collection<PredicateWithArity>,
  ): RulesDatabase = MapRulesDatabase(map.filterKeys { it in keys })

  override fun contains(key: PredicateWithArity): Boolean = map.containsKey(key)
}
