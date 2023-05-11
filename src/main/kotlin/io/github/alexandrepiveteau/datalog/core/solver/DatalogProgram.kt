package io.github.alexandrepiveteau.datalog.core.solver

import io.github.alexandrepiveteau.datalog.core.*
import io.github.alexandrepiveteau.datalog.core.Relation as CoreRelation
import io.github.alexandrepiveteau.datalog.core.algebra.Relation
import io.github.alexandrepiveteau.datalog.core.algebra.buildRelation
import io.github.alexandrepiveteau.datalog.core.interpreter.EDB
import io.github.alexandrepiveteau.datalog.core.interpreter.IDB
import io.github.alexandrepiveteau.datalog.core.interpreter.naiveEval

class DatalogProgram : ProgramBuilder, Program {

  private var nextRelation = 0
  private var nextVariable = -1
  private var nextConstant = 0

  override fun relation(): CoreRelation = CoreRelation(nextRelation++)
  override fun variable(): Atom = Atom(nextVariable--)
  override fun constant(): Atom = Atom(nextConstant++)

  private val rules = mutableListOf<Rule>()

  override fun rule(relation: CoreRelation, atoms: AtomList, block: RuleBuilder.() -> Unit) {
    val clauses = buildList {
      val builder = RuleBuilder { r, a, n -> add(Clause(r, a, n)) }
      block(builder)
    }
    rules.add(Rule(relation, atoms, clauses))
  }

  override fun build(): Program = this

  override fun solve(relation: CoreRelation): Iterable<Fact> {
    val (idb, edb) = partition(rules)
    val result = naiveEval(idb, edb)
    val facts = result[relation] ?: return emptyList()
    return facts.mapToFacts(relation).asIterable()
  }
}

private fun partition(rules: Collection<Rule>): Pair<IDB, EDB> {
  val edbBuilder = mutableMapOf<CoreRelation, MutableSet<AtomList>>()
  val idbBuilder = mutableMapOf<CoreRelation, MutableSet<Rule>>()

  for (rule in rules) {
    if (rule.clauses.isEmpty()) {
      edbBuilder.getOrPut(rule.relation) { mutableSetOf() } += rule.atoms
    } else {
      idbBuilder.getOrPut(rule.relation) { mutableSetOf() } += rule
    }
  }

  val edb =
      edbBuilder.mapValues { (_, atoms) ->
        val arity = atoms.first().size
        buildRelation(arity) { for (atom in atoms) yield(atom) }
      }

  return idbBuilder to edb
}

private fun Relation.mapToFacts(
    relation: CoreRelation,
): Sequence<Fact> = sequence { tuples.forEach { yield(Fact(relation, it)) } }
