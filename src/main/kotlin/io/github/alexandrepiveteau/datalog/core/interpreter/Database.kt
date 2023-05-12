package io.github.alexandrepiveteau.datalog.core.interpreter

import io.github.alexandrepiveteau.datalog.core.AtomList
import io.github.alexandrepiveteau.datalog.core.Predicate
import io.github.alexandrepiveteau.datalog.core.interpreter.algebra.buildRelation

/**
 * Partitions the given collection of rules in an [IDB] and an [EDB] pair, based on the presence of
 * clauses in the rules.
 *
 * @param rules the [Collection] of [PredicateRule]s to partition.
 * @return a pair of [IDB] and [EDB] instances.
 */
internal fun partition(rules: Collection<PredicateRule>): Pair<IDB, EDB> {
  val edbBuilder = mutableMapOf<Predicate, MutableSet<AtomList>>()
  val idbBuilder = mutableMapOf<Predicate, MutableSet<PredicateRule>>()

  for (rule in rules) {
    if (rule.clauses.isEmpty()) {
      edbBuilder.getOrPut(rule.predicate) { mutableSetOf() } += rule.atoms
    } else {
      idbBuilder.getOrPut(rule.predicate) { mutableSetOf() } += rule
    }
  }

  val edb =
      edbBuilder.mapValuesTo(mutableMapOf()) { (_, atoms) ->
        val arity = atoms.first().size
        buildRelation(arity) { for (atom in atoms) yield(atom) }
      }

  return MapIDB(idbBuilder) to MutableMapEDB2(edb)
}
