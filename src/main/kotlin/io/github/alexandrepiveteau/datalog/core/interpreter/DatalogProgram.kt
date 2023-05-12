package io.github.alexandrepiveteau.datalog.core.interpreter

import io.github.alexandrepiveteau.datalog.core.*
import io.github.alexandrepiveteau.datalog.core.interpreter.algebra.Relation
import io.github.alexandrepiveteau.datalog.core.interpreter.algebra.buildRelation

/**
 * An implementation of [Program] and [ProgramBuilder] which executes the rules with the naive
 * Datalog evaluation algorithm.
 */
internal class DatalogProgram : ProgramBuilder, Program {

  private var nextRelation = 0
  private var nextVariable = -1
  private var nextConstant = 0

  override fun predicate(): Predicate = Predicate(nextRelation++)
  override fun variable(): Atom = Atom(nextVariable--)
  override fun constant(): Atom = Atom(nextConstant++)

  private val rules = mutableListOf<Rule>()

  override fun rule(predicate: Predicate, atoms: AtomList, block: RuleBuilder.() -> Unit) {
    val clauses = buildList {
      val builder = RuleBuilder { r, a, n -> add(Clause(r, a, n)) }
      block(builder)
    }
    rules.add(Rule(predicate, atoms, clauses))
  }

  override fun build(): Program = this

  private fun context() = Context(sequence { for (i in 0 until nextConstant) yield(Atom(i)) })

  override fun solve(predicate: Predicate): Iterable<Fact> {
    val (idb, edb) = partition(rules)
    val result = with(context()) { stratifiedEval(idb, edb, ::naiveEval) }
    val facts = result[predicate] ?: return emptyList()
    return facts.mapToFacts(predicate).asIterable()
  }
}

/**
 * A [Rule] describes how some facts can be derived in a [Predicate], given some other facts in
 * other [Predicate]s. The [clauses] indicate the relations that this [Rule] depends on, and the
 * head of the rule is represented by its [atoms].
 */
internal data class Rule(
    val predicate: Predicate,
    val atoms: AtomList,
    val clauses: List<Clause>,
)

/** A [Clause] is a part of a rule which describes a [Predicate] that a [Rule] depends on. */
internal data class Clause(
    val predicate: Predicate,
    val atoms: AtomList,
    val negated: Boolean,
)

/**
 * Partitions the given collection of rules in an [IDB] and an [EDB] pair, based on the presence of
 * clauses in the rules.
 *
 * @param rules the [Collection] of [Rule]s to partition.
 * @return a pair of [IDB] and [EDB] instances.
 */
private fun partition(rules: Collection<Rule>): Pair<IDB, EDB> {
  val edbBuilder = mutableMapOf<Predicate, MutableSet<AtomList>>()
  val idbBuilder = mutableMapOf<Predicate, MutableSet<Rule>>()

  for (rule in rules) {
    if (rule.clauses.isEmpty()) {
      edbBuilder.getOrPut(rule.predicate) { mutableSetOf() } += rule.atoms
    } else {
      idbBuilder.getOrPut(rule.predicate) { mutableSetOf() } += rule
    }
  }

  val edb =
      edbBuilder.mapValues { (_, atoms) ->
        val arity = atoms.first().size
        buildRelation(arity) { for (atom in atoms) yield(atom) }
      }

  return idbBuilder to edb
}

/** Transforms a [Relation] to a [Sequence] of [Fact]s, which can be output back. */
private fun Relation.mapToFacts(
    predicate: Predicate,
): Sequence<Fact> = sequence { tuples.forEach { yield(Fact(predicate, it)) } }
