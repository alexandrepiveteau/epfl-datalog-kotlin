package io.github.alexandrepiveteau.datalog.core.interpreter.database

import io.github.alexandrepiveteau.datalog.core.rule.CombinationRule
import io.github.alexandrepiveteau.datalog.core.rule.Rule

/**
 * An intentional database, with predicate derivation rules. Each set of rules is associated with a
 * predicate and an arity. The same predicate can be associated with a different set of rules and
 * may have different arity.
 */
internal interface RulesDatabase<out T> {

  /** Returns an [Iterator] with all keys in the [RulesDatabase]. */
  operator fun iterator(): Iterator<PredicateWithArity>

  /** Returns the [Set] of [CombinationRule]s for the given [PredicateWithArity]. */
  operator fun get(key: PredicateWithArity): Set<Rule<T>>

  /**
   * Returns a new [RulesDatabase] with the same contents as this [RulesDatabase] for the given keys
   * only.
   */
  fun filter(keys: Collection<PredicateWithArity>): RulesDatabase<T>

  /**
   * Returns `true` if the [RulesDatabase] contains the given [PredicateWithArity], or `false`
   * otherwise.
   */
  operator fun contains(key: PredicateWithArity): Boolean
}

internal data class MapRulesDatabase<out T>(
    private val map: Map<PredicateWithArity, Set<Rule<T>>>,
) : RulesDatabase<T> {

  override fun iterator(): Iterator<PredicateWithArity> = map.keys.iterator()

  override fun get(key: PredicateWithArity): Set<Rule<T>> = map[key] ?: emptySet()

  override fun filter(
      keys: Collection<PredicateWithArity>,
  ): RulesDatabase<T> = MapRulesDatabase(map.filterKeys { it in keys })

  override fun contains(key: PredicateWithArity): Boolean = map.containsKey(key)
}
