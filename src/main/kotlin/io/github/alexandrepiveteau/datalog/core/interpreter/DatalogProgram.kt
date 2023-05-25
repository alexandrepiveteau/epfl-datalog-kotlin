package io.github.alexandrepiveteau.datalog.core.interpreter

import io.github.alexandrepiveteau.datalog.core.*
import io.github.alexandrepiveteau.datalog.core.interpreter.algebra.Relation
import io.github.alexandrepiveteau.datalog.core.interpreter.database.FactsDatabase
import io.github.alexandrepiveteau.datalog.core.interpreter.database.PredicateWithArity
import io.github.alexandrepiveteau.datalog.core.interpreter.database.RulesDatabase

/**
 * An implementation of [Program] to obtain results.
 *
 * @param domain the [Domain] of the program.
 * @param rules the [Rule]s of the program.
 * @param nextConstant the index of the next constant that would be created.
 * @param evalStrata the function used to evaluate each stratum.
 */
internal class DatalogProgram(
    private val domain: Domain,
    private val rules: MutableSet<Rule>,
    private val nextConstant: Int,
    private val evalStrata: (Context) -> (RulesDatabase, FactsDatabase) -> FactsDatabase,
) : Program {

  private fun context() =
      Context(
          atoms = sequence { for (i in 0 until nextConstant) yield(Atom(i)) },
          domain = domain,
      )

  override fun solve(predicate: Predicate, arity: Int): Iterable<Fact> {
    val (idb, edb) = partition(rules)
    val target = PredicateWithArity(predicate, arity)
    val result = stratifiedEval(target, idb, edb, evalStrata(context()))
    val facts = result[target]
    return facts.mapToFacts(predicate).asIterable()
  }
}

/** Transforms a [Relation] to a [Sequence] of [Fact]s, which can be output back. */
private fun Relation.mapToFacts(
    predicate: Predicate,
): Sequence<Fact> = sequence { tuples.forEach { yield(Fact(predicate, it)) } }
