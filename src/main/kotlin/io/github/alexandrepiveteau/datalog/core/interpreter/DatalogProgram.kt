package io.github.alexandrepiveteau.datalog.core.interpreter

import io.github.alexandrepiveteau.datalog.core.*
import io.github.alexandrepiveteau.datalog.core.interpreter.algebra.Relation

/**
 * An implementation of [Program] and [ProgramBuilder] which executes the rules with the naive
 * Datalog evaluation algorithm.
 *
 * @param evalStrata the function used to evaluate each stratum.
 */
internal class DatalogProgram
private constructor(
    private val evalStrata: (Context) -> (IDB, EDB) -> EDB,
) : ProgramBuilder, Program {

  private var nextRelation = 0
  private var nextVariable = -1
  private var nextConstant = 0

  override fun predicate(): Predicate = Predicate(nextRelation++)
  override fun variable(): Atom = Atom(nextVariable--)
  override fun constant(): Atom = Atom(nextConstant++)

  private val rules = mutableListOf<PredicateRule>()

  override fun rule(predicate: Predicate, atoms: AtomList, block: RuleBuilder.() -> Unit) {
    val clauses = buildList {
      val builder = RuleBuilder { r, a, n -> add(PredicateClause(r, a, n)) }
      block(builder)
    }
    rules.add(PredicateRule(predicate, atoms, clauses))
  }

  override fun build(): Program = this

  private fun context() = Context(sequence { for (i in 0 until nextConstant) yield(Atom(i)) })

  override fun solve(predicate: Predicate): Iterable<Fact> {
    val (idb, edb) = partition(rules)
    val result = stratifiedEval(idb, edb, evalStrata(context()))
    val facts = result[predicate] ?: return emptyList()
    return facts.mapToFacts(predicate).asIterable()
  }

  companion object {

    /** Returns a [DatalogProgram] which executes using naive evaluation. */
    fun naive(): DatalogProgram = DatalogProgram { with(it) { ::naiveEval } }

    /** Returns a [DatalogProgram] which executes using semi-naive evaluation. */
    fun semiNaive(): DatalogProgram = DatalogProgram { with(it) { ::semiNaiveEval } }
  }
}

/** Transforms a [Relation] to a [Sequence] of [Fact]s, which can be output back. */
private fun Relation.mapToFacts(
    predicate: Predicate,
): Sequence<Fact> = sequence { tuples.forEach { yield(Fact(predicate, it)) } }
