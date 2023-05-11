package io.github.alexandrepiveteau.datalog.dsl

/**
 * The scope in which Datalog programs are handled. The domain of the program is defined as the set
 * of values that appear in the rules or the facts, and automatically inferred from the program.
 *
 * TODO : Provide an API to let users define their domain explicitly.
 *
 * @param T the type of the elements in the relations.
 */
interface DatalogScope<T> {

  /**
   * Returns some [Terms] which are guaranteed to be empty. This is useful when we want to create a
   * new rule with no terms on the right-hand side.
   */
  val empty: Terms<Nothing>
    get() = Terms(emptySet())

  /** Registers the set of constants in the [DatalogScope]. */
  fun constants(vararg values: T)

  /** Returns a new [Variable] which is guaranteed to be unique within this [DatalogScope]. */
  fun variable(): Variable<T>

  /** Returns a [Variables] instance. */
  fun variables(): Variables<T> = Variables { variable() }

  /** Returns a new [Relation] which is guaranteed to be unique within this [DatalogScope]. */
  fun relation(): Relation<T>

  /** Returns a [Relations] instance. */
  fun relations(): Relations<T> = Relations { relation() }

  // Utilities for creating terms.
  fun <R> R.asValue(): Value<R> = Value(this)

  // Relation to term operators.
  operator fun Relation<T>.invoke(vararg atoms: T) = Term(this, atoms.map { Value(it) }, false)
  operator fun Relation<T>.invoke(vararg atoms: Atom<T>) = Term(this, atoms.toList(), false)
  operator fun Term<T>.not() = copy(negated = !negated)

  // Terms operators.
  operator fun Term<T>.plus(term: Term<T>): Terms<T> = Terms(setOf(this, term))
  operator fun Term<T>.plus(terms: Terms<T>): Terms<T> = Terms(setOf(this) + terms.terms)
  operator fun Terms<T>.plus(term: Term<T>): Terms<T> = Terms(this.terms + term)
  operator fun Terms<T>.plus(terms: Terms<T>): Terms<T> = Terms(this.terms + terms.terms)

  // Rules operators.
  operator fun Term<T>.plusAssign(terms: Terms<T>)
  operator fun Term<T>.plusAssign(term: Term<T>) = plusAssign(term + empty)

  /**
   * Derives a new [Set] of [Term] that are implied by the given [relation] and the current set of
   * rules. If the semantics of the program is not well-defined, this function may return an empty
   * set.
   *
   * @param relation the relation to solve.
   */
  fun solve(relation: Relation<T>): Set<Term<T>>
}
