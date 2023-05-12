package io.github.alexandrepiveteau.datalog.core.interpreter

import io.github.alexandrepiveteau.datalog.core.Predicate
import io.github.alexandrepiveteau.datalog.core.interpreter.algebra.Relation
import io.github.alexandrepiveteau.datalog.core.interpreter.algebra.distinct
import io.github.alexandrepiveteau.datalog.core.interpreter.algebra.empty
import io.github.alexandrepiveteau.datalog.core.interpreter.algebra.union

/** An interface representing an extensional database, with facts. */
internal interface EDB {

  /** Returns an [Iterator] with all predicates in the [IDB]. */
  operator fun iterator(): Iterator<Predicate>

  /** Returns the [Relation] associated to the given [Predicate]. */
  operator fun get(predicate: Predicate): Relation?

  /** Adds the values present in both [EDB]s and returns a new [EDB]. */
  operator fun plus(other: EDB): EDB

  /** Returns a [MutableEDB] with the same contents as this [EDB]. */
  fun toMutableEDB(): MutableEDB
}

/** A mutable variant of [EDB]. */
internal interface MutableEDB : EDB {

  /** Sets the [Relation] for the given [Predicate]. */
  operator fun set(predicate: Predicate, relation: Relation)
}

/** An implementation of [EDB] backed by a [MutableMap]. */
internal data class MutableMapEDB2(private val map: MutableMap<Predicate, Relation>) : MutableEDB {
  override fun set(predicate: Predicate, relation: Relation) = map.set(predicate, relation)
  override fun iterator(): Iterator<Predicate> = map.keys.iterator()
  override fun get(predicate: Predicate) = map[predicate]
  override fun plus(other: EDB): EDB {
    val result = map.toMutableMap()
    for (predicate in other) {
      val relation = other[predicate] ?: continue
      val existing = result.getOrPut(predicate) { Relation.empty(relation.arity) }
      result[predicate] = existing.union(relation).distinct()
    }
    return MutableMapEDB2(result)
  }
  override fun toMutableEDB(): MutableEDB = MutableMapEDB2(map.toMutableMap())
}