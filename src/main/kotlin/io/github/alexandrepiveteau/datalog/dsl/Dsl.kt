package io.github.alexandrepiveteau.datalog.dsl

import io.github.alexandrepiveteau.datalog.core.Algorithm
import io.github.alexandrepiveteau.datalog.core.Atom as CoreAtom
import io.github.alexandrepiveteau.datalog.core.Domain as CoreDomain
import io.github.alexandrepiveteau.datalog.core.NoSuchAtomException
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
inline fun <T, R> datalog(
    domain: Domain<T>,
    algorithm: Algorithm = Algorithm.Naive,
    scope: DatalogScope<T>.() -> R
): R = datalog(domain, algorithm).scope()

/** Returns a [DatalogScope] instance, which can be used for the DSL. */
@PublishedApi
internal fun <T> datalog(
    domain: Domain<T>,
    algorithm: Algorithm,
): DatalogScope<T> = Datalog(domain, algorithm)

// IMPLEMENTATION

/** An interface representing a translation table between two kinds of symbols. */
private interface Translation<K, V> {

  /** Returns the [V] corresponding to the given [K]. */
  fun getValue(value: K): V

  /** Returns the [K] corresponding to the given [V]. */
  fun getKey(value: V): K

  /** Returns a [Translation] that is immutable, and known not to generate symbols. */
  fun toImmutableTranslation(): Translation<K, V>
}

private class GeneratorTranslation<K, V>(private val generator: () -> V) : Translation<K, V> {
  private val toMap = mutableMapOf<K, V>()
  private val fromMap = mutableMapOf<V, K>()

  override fun getValue(value: K): V {
    val id = toMap.getOrPut(value) { generator() }
    fromMap[id] = value
    return id
  }

  override fun getKey(value: V): K {
    return fromMap[value] ?: throw NoSuchAtomException()
  }

  override fun toImmutableTranslation(): Translation<K, V> {
    return object : Translation<K, V> {
      override fun getValue(value: K): V = toMap[value] ?: throw NoSuchAtomException()
      override fun getKey(value: V): K = fromMap[value] ?: throw NoSuchAtomException()
      override fun toImmutableTranslation(): Translation<K, V> = this
    }
  }
}

private class ActualDomain<T>(
    private val domain: Domain<T>,
    private val translation: Translation<T, CoreAtom>,
) : CoreDomain {

  override fun unit(): CoreAtom = translation.getValue(domain.unit())

  override fun sum(
      a: CoreAtom,
      b: CoreAtom,
  ): CoreAtom = translation.getValue(domain.sum(translation.getKey(a), translation.getKey(b)))

  override fun max(
      a: CoreAtom,
      b: CoreAtom,
  ): CoreAtom = translation.getValue(domain.max(translation.getKey(a), translation.getKey(b)))

  override fun min(
      a: CoreAtom,
      b: CoreAtom,
  ): CoreAtom = translation.getValue(domain.min(translation.getKey(a), translation.getKey(b)))
}

private class Datalog<T>(private val domain: Domain<T>, algorithm: Algorithm) : DatalogScope<T> {
  private val translation = GeneratorTranslation<T, CoreAtom>(this::constant)
  private val builder = CoreProgramBuilder(algorithm)

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

  private fun CoreAtom.translate(
      translation: Translation<T, CoreAtom> = this@Datalog.translation,
  ): Atom<T> {
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
              a.columns.map { it.translate() }.asAtomList(),
              a.result.translate(),
          )
        }
  }

  override fun solve(predicate: Predicate<T>, arity: Int): Set<Term<T>> {
    val translation = translation.toImmutableTranslation()
    return builder
        .build(ActualDomain(domain, translation))
        .solve(predicate.id, arity)
        .asSequence()
        .map {
          Term(
              predicate = Predicate(it.predicate),
              atoms = it.atoms.map { atom -> atom.translate(translation) },
              negated = false,
          )
        }
        .toSet()
  }
}
