package io.github.alexandrepiveteau.datalog.core.interpreter.database

import io.github.alexandrepiveteau.datalog.core.Fact
import io.github.alexandrepiveteau.datalog.core.interpreter.algebra.Relation
import io.github.alexandrepiveteau.datalog.core.interpreter.algebra.distinct
import io.github.alexandrepiveteau.datalog.core.interpreter.algebra.empty
import io.github.alexandrepiveteau.datalog.core.interpreter.algebra.union

// TODO : Document this.
internal interface FactsDatabaseBuilder<T> : FactsDatabaseBuilderScope<T> {
  fun build(): FactsDatabase<T>
}

// TODO : Document this.
internal fun <T> buildFactsDatabase(
    builder: FactsDatabaseBuilderScope<T>.() -> Unit,
): FactsDatabase<T> = MutableFactsDatabase.builder<T>().apply(builder).build()

// TODO : Document this.
internal fun <T> MutableFactsDatabase.Companion.empty(): MutableFactsDatabase<T> =
    MapMutableFactsDatabaseBuilder(mutableMapOf())

// TODO : Document this.
internal fun <T> MutableFactsDatabase.Companion.builder(): FactsDatabaseBuilder<T> {
  return object : FactsDatabaseBuilder<T> {
    private val map = mutableMapOf<PredicateWithArity, MutableSet<Fact<T>>>()
    override fun add(predicate: PredicateWithArity, fact: Fact<T>) {
      if (predicate.arity != fact.size) throw IllegalArgumentException("Invalid arity")
      map.getOrPut(predicate) { mutableSetOf() }.add(fact)
    }
    override fun build(): FactsDatabase<T> =
        MapMutableFactsDatabaseBuilder(
            map.mapValuesTo(mutableMapOf()) { (k, v) -> Relation(k.arity, v) },
        )
  }
}

// TODO : Document this.
private data class MapMutableFactsDatabaseBuilder<T>(
    private val map: MutableMap<PredicateWithArity, Relation<T>>,
) : MutableFactsDatabase<T> {

  override fun plusAssign(other: FactsDatabase<T>) {
    for (predicate in other) map[predicate] = this[predicate].union(other[predicate]).distinct()
  }

  override fun set(predicate: PredicateWithArity, relation: Relation<T>) =
      map.set(predicate, relation)

  override fun iterator() = map.keys.iterator()

  override fun get(
      predicate: PredicateWithArity,
  ) = map[predicate] ?: Relation.empty(predicate.arity)

  override fun plus(
      other: FactsDatabase<T>,
  ) = toMutableFactDatabase().apply { plusAssign(other) }

  override fun toMutableFactDatabase() = MapMutableFactsDatabaseBuilder(map.toMutableMap())
}
