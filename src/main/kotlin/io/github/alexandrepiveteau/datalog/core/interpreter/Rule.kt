package io.github.alexandrepiveteau.datalog.core.interpreter

import io.github.alexandrepiveteau.datalog.core.Predicate
import io.github.alexandrepiveteau.datalog.core.RuleBuilder
import io.github.alexandrepiveteau.datalog.dsl.Atom
import io.github.alexandrepiveteau.datalog.dsl.Variable

/** A [Rule] represents some kind of derivation used to derive new facts from existing ones. */
internal sealed interface Rule<out T> : Term<T> {

  /** The [Clause]s of the [Rule]. */
  val clauses: List<Clause<T>>
}

/** A [Term] is a [Predicate] with some atoms. */
interface Term<out T> {

  /** The [Predicate] of the [Term]. */
  val predicate: Predicate

  /** The projection of the [Term] as an [AtomList]. */
  val atoms: List<Atom<T>>

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
internal data class CombinationRule<out T>(
    override val predicate: Predicate,
    override val atoms: List<Atom<T>>,
    override val clauses: List<Clause<T>>,
) : Rule<T>

/**
 * A [Clause] defines which parts of a relation should be matched when deriving some new facts. The
 * [atoms] define the pattern that should be matched, and the [negated] flag indicates whether the
 * pattern should be negated or not.
 *
 * @param predicate the [Predicate] of the clause.
 * @param atoms the pattern that should be matched.
 * @param negated true if the pattern should be negated, false otherwise
 */
internal data class Clause<out T>(
    override val predicate: Predicate,
    override val atoms: List<Atom<T>>,
    val negated: Boolean,
) : Term<T>

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
internal data class AggregationRule<out T>(
    override val predicate: Predicate,
    override val atoms: List<Atom<T>>,
    val clause: Clause<T>,
    val operator: RuleBuilder.Aggregate,
    val same: Collection<Variable<T>>,
    val columns: Collection<Variable<T>>,
    val result: Variable<T>,
) : Rule<T> {

  override val clauses: List<Clause<T>>
    get() = listOf(clause)
}
