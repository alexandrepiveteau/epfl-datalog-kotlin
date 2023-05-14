package io.github.alexandrepiveteau.datalog.core.interpreter.database

import io.github.alexandrepiveteau.datalog.core.AtomList
import io.github.alexandrepiveteau.datalog.core.interpreter.algebra.Relation
import io.github.alexandrepiveteau.datalog.core.interpreter.algebra.distinct
import io.github.alexandrepiveteau.datalog.core.interpreter.algebra.empty
import io.github.alexandrepiveteau.datalog.core.interpreter.algebra.union

// TODO : Document this.
internal interface FactsDatabaseBuilder : FactsDatabaseBuilderScope {
  fun build(): FactsDatabase
}

// TODO : Document this.
internal fun buildFactsDatabase(
    builder: FactsDatabaseBuilderScope.() -> Unit,
): FactsDatabase = MutableFactsDatabase.builder().apply(builder).build()

// TODO : Document this.
internal fun MutableFactsDatabase.Companion.empty(): MutableFactsDatabase =
    MapMutableFactsDatabaseBuilder(mutableMapOf())

// TODO : Document this.
internal fun MutableFactsDatabase.Companion.builder(): FactsDatabaseBuilder {
  return object : FactsDatabaseBuilder {
    private val map = mutableMapOf<PredicateWithArity, MutableSet<AtomList>>()
    override fun add(predicate: PredicateWithArity, fact: AtomList) {
      if (predicate.arity != fact.size) throw IllegalArgumentException("Invalid arity")
      map.getOrPut(predicate) { mutableSetOf() }.add(fact)
    }
    override fun build(): FactsDatabase =
        MapMutableFactsDatabaseBuilder(
            map.mapValuesTo(mutableMapOf()) { (k, v) -> Relation(k.arity, v) },
        )
  }
}

// TODO : Document this.
private data class MapMutableFactsDatabaseBuilder(
    private val map: MutableMap<PredicateWithArity, Relation>,
) : MutableFactsDatabase {

  override fun plusAssign(other: FactsDatabase) {
    for (predicate in other) map[predicate] = this[predicate].union(other[predicate]).distinct()
  }

  override fun set(predicate: PredicateWithArity, relation: Relation) = map.set(predicate, relation)

  override fun iterator() = map.keys.iterator()

  override fun get(
      predicate: PredicateWithArity,
  ) = map[predicate] ?: Relation.empty(predicate.arity)

  override fun plus(
      other: FactsDatabase,
  ) = toMutableFactDatabase().apply { plusAssign(other) }

  override fun toMutableFactDatabase() = MapMutableFactsDatabaseBuilder(map.toMutableMap())
}
