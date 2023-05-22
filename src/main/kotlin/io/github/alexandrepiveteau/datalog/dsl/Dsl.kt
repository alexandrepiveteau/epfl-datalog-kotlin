package io.github.alexandrepiveteau.datalog.dsl

import io.github.alexandrepiveteau.datalog.core.Algorithm
import io.github.alexandrepiveteau.datalog.core.Atom as CoreAtom
import io.github.alexandrepiveteau.datalog.core.Domain
import io.github.alexandrepiveteau.datalog.core.ProgramBuilder as CoreProgramBuilder
import io.github.alexandrepiveteau.datalog.core.asAtomList
import io.github.alexandrepiveteau.datalog.core.map

/**
 * Runs a Datalog program within the given [scope] and returns the result. The [scope] is a
 * [DatalogScope] receiver.
 *
 * @param algorithm the [Algorithm] to use for solving the Datalog program.
 * @param T the type of the elements in the relations.
 * @param R the return type of the Datalog program.
 * @param scope the scope in which the Datalog program is run.
 * @return the result of the Datalog program.
 */
inline fun <T : Comparable<T>, R> datalog(
    algorithm: Algorithm = Algorithm.Naive,
    scope: DatalogScope<T>.() -> R
): R = datalog<T>(algorithm).scope()

/** Returns a [DatalogScope] instance, which can be used for the DSL. */
@PublishedApi
internal fun <T : Comparable<T>> datalog(
    algorithm: Algorithm,
): DatalogScope<T> = Datalog(algorithm)

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

private class ComparableDomain<T : Comparable<T>>(
    private val translation: Translation<T, CoreAtom>,
) : Domain {

  override fun max(
      a: CoreAtom,
      b: CoreAtom,
  ): CoreAtom = translation.getValue(maxOf(translation.getKey(a), translation.getKey(b)))

  override fun min(
      a: CoreAtom,
      b: CoreAtom,
  ): CoreAtom = translation.getValue(minOf(translation.getKey(a), translation.getKey(b)))
}

private class Datalog<T : Comparable<T>>(algorithm: Algorithm) : DatalogScope<T> {
  private val translation = Translation<T, CoreAtom>(this::constant)
  private val builder = CoreProgramBuilder(algorithm, ComparableDomain(translation))

  // This function is needed because of the cross-references between `translation` and `builder`.
  private fun constant(): CoreAtom = builder.constant()

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
    return builder.rule(
        predicate = predicate.id, atoms = atoms.map { it.translate() }.asAtomList()) {
          for ((relation, atoms, negated) in terms.terms) {
            predicate(relation.id, atoms.map { it.translate() }.asAtomList(), negated)
          }
        }
  }

  override fun Term<T>.plusAssign(aggregation: Aggregation<T>) {
    return builder.rule(
        predicate = predicate.id, atoms = atoms.map { it.translate() }.asAtomList()) {
          val (p, a) = aggregation
          predicate(p.predicate.id, p.atoms.map { it.translate() }.asAtomList(), p.negated)
          aggregate(
              a.aggregate,
              a.same.map { it.translate() }.asAtomList(),
              a.column.translate(),
              a.result.translate(),
          )
        }
  }

  override fun solve(predicate: Predicate<T>, arity: Int): Set<Term<T>> {
    return builder
        .build()
        .solve(predicate.id, arity)
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
