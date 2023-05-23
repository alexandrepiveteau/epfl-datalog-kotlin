package io.github.alexandrepiveteau.datalog.core.interpreter

import io.github.alexandrepiveteau.datalog.core.Atom
import io.github.alexandrepiveteau.datalog.core.AtomList
import io.github.alexandrepiveteau.datalog.core.Predicate
import io.github.alexandrepiveteau.datalog.core.RuleBuilder

/** A [Rule] represents some kind of derivation used to derive new facts from existing ones. */
internal sealed interface Rule : Term {

  /** The [Clause]s of the [Rule]. */
  val clauses: List<Clause>
}

/** A [Term] is a [Predicate] with some atoms. */
interface Term {

  /** The [Predicate] of the [Term]. */
  val predicate: Predicate

  /** The projection of the [Term] as an [AtomList]. */
  val atoms: AtomList

  /** Returns the [Int] arity of the [Term]. */
  val arity: Int
    get() = atoms.size
}

/**
 * A [CombinationRule] defines the derivation of a new relation, following the pattern defined by
 * the [AtomList] for production and using the [Clause]s for the derivation.
 *
 * @param predicate the [Predicate] of the rule.
 * @param atoms the pattern that should be produced by the [CombinationRule].
 * @param clauses the [List] of [Clause]s that should be used to derive some new facts.
 */
internal data class CombinationRule(
    override val predicate: Predicate,
    override val atoms: AtomList,
    override val clauses: List<Clause>,
) : Rule

/**
 * A [Clause] defines which parts of a relation should be matched when deriving some new facts. The
 * [atoms] define the pattern that should be matched, and the [negated] flag indicates whether the
 * pattern should be negated or not.
 *
 * @param predicate the [Predicate] of the clause.
 * @param atoms the pattern that should be matched.
 * @param negated true if the pattern should be negated, false otherwise
 */
internal data class Clause(
    override val predicate: Predicate,
    override val atoms: AtomList,
    val negated: Boolean,
) : Term

/**
 * An [AggregationRule] defines the derivation of a new relation, following the pattern defined by
 * the [AtomList] for production and using the given [Clause] for derivation.
 *
 * @param predicate the [Predicate] of the rule.
 * @param atoms the pattern that should be produced by the [AggregationRule].
 * @param clause the [Clause] that should be used to derive some new facts.
 * @param operator the [RuleBuilder.Aggregate] operator that should be used to aggregate the [same]
 *   atoms.
 * @param same the [AtomList] that should be used to aggregate the [columns] atoms.
 * @param columns the variable [Atom] that should be aggregated.
 * @param result the variable [Atom] that should be used to store the result of the aggregation.
 */
internal data class AggregationRule(
    override val predicate: Predicate,
    override val atoms: AtomList,
    val clause: Clause,
    val operator: RuleBuilder.Aggregate,
    val same: AtomList,
    val columns: AtomList,
    val result: Atom,
) : Rule {

  override val clauses: List<Clause>
    get() = listOf(clause)
}
