package io.github.alexandrepiveteau.datalog.dsl

import io.github.alexandrepiveteau.datalog.core.Atom as CoreAtom
import io.github.alexandrepiveteau.datalog.core.ProgramBuilder as CoreProgramBuilder
import io.github.alexandrepiveteau.datalog.core.asAtomList
import io.github.alexandrepiveteau.datalog.core.map

/**
 * Runs a Datalog program within the given [scope] and returns the result. The [scope] is a
 * [DatalogScope] receiver.
 *
 * @param T the type of the elements in the relations.
 * @param R the return type of the Datalog program.
 * @param scope the scope in which the Datalog program is run.
 * @return the result of the Datalog program.
 */
inline fun <T, R> datalog(scope: DatalogScope<T>.() -> R): R = datalog<T>().scope()

/** Returns a [DatalogScope] instance, which can be used for the DSL. */
@PublishedApi internal fun <T> datalog(): DatalogScope<T> = Datalog()

// IMPLEMENTATION

private class Translation<K, V>(private val generator: () -> V) {
  private val toMap = mutableMapOf<K, V>()
  private val fromMap = mutableMapOf<V, K>()

  fun getValue(value: K): V {
    val id = toMap.getOrPut(value) { generator() }
    fromMap[id] = value
    return id
  }

  fun getKey(value: V): K {
    return fromMap[value] ?: error("Unknown value $value.")
  }
}

private class Datalog<T> : DatalogScope<T> {
  private val builder = CoreProgramBuilder()
  private val translation = Translation<T, CoreAtom>(builder::constant)

  override fun constants(vararg values: T) {
    for (constant in values) translation.getValue(constant)
  }
  override fun variable() = Variable<T>(builder.variable())
  override fun predicate() = Predicate<T>(builder.predicate())

  private fun Atom<T>.translate(): CoreAtom {
    return when (this) {
      is Value -> translation.getValue(this.value)
      is Variable -> this.atom
    }
  }

  private fun CoreAtom.translate(): Atom<T> {
    return if (isConstant) Value(translation.getKey(this)) else Variable(this)
  }

  override fun Term<T>.plusAssign(terms: Terms<T>) {
    return builder.rule(predicate = predicate.id, atoms = atoms.map { it.translate() }.asAtomList()) {
      for ((relation, atoms, negated) in terms.terms) {
        body(relation.id, atoms.map { it.translate() }.asAtomList(), negated)
      }
    }
  }

  override fun solve(predicate: Predicate<T>): Set<Term<T>> {
    return builder
        .build()
        .solve(predicate.id)
        .asSequence()
        .map {
          Term(
              predicate = Predicate(it.predicate),
              atoms = it.atoms.map { atom -> atom.translate() },
              negated = false,
          )
        }
        .toSet()
  }
}
