package io.github.alexandrepiveteau.datalog.dsl

/**
 * An interface representing an atom in Datalog. An atom can either be a [Value] or [Variable], and
 * may appear in a [Term] when it is used as a predicate argument.
 *
 * @param T the type of the elements in the relation.
 */
sealed interface Atom<out T>

/**
 * A class representing a value [Atom].
 *
 * @param T the type of the elements in the relation.
 * @param value the value of the atom.
 */
data class Value<out T>(val value: T) : Atom<T>

/**
 * A class representing a variable [Atom].
 *
 * @param T the type of the elements in the relation.
 * @param id the unique identifier of the variable, which gives its identity to the variable.
 */
// TODO : Make this an opaque type.
data class Variable<out T>(val id: Int) : Atom<T>

/**
 * A [Term] which contains a list of [Atom]s. The term might be negated.
 *
 * @param T the type of the elements in the relation.
 * @param relation the relation of this term.
 * @param atoms the atoms in this term.
 * @param negated true iff this term is negated.
 */
data class Term<out T>(val relation: Relation<T>, val atoms: List<Atom<T>>, val negated: Boolean)

/**
 * A class representing an unordered set of [Term]s. This is useful when we want to create a new
 * rule with multiple terms on the right-hand side.
 *
 * @param T the type of the elements in the relation.
 * @param terms the terms in this set.
 */
data class Terms<out T>(val terms: Set<Term<T>>)

/**
 * A class representing a relation of multiple terms.
 *
 * @param T the type of the elements in the relation.
 * @param id the unique identifier of the relation, which gives its identity to the relation.
 */
// TODO : Make this an opaque type.
data class Relation<out T>(val id: Int)

/**
 * The scope in which Datalog programs are handled.
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

  /** Returns a new [Variable] which is guaranteed to be unique within this [DatalogScope]. */
  fun variable(): Variable<T>

  /** Returns a [Variables] instance. */
  fun variables(): Variables<T> = Variables { variable() }

  /** Returns a new [Relation] which is guaranteed to be unique within this [DatalogScope]. */
  fun relation(): Relation<T>

  /** Returns a [Relations] instance. */
  fun relations(): Relations<T> = Relations { relation() }

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

/** A [Relations] is a helper class which allows to create multiple [Relation]s at once. */
fun interface Relations<out T> {
  operator fun invoke(): Relation<T>
  operator fun component1(): Relation<T> = invoke()
  operator fun component2(): Relation<T> = invoke()
  operator fun component3(): Relation<T> = invoke()
  operator fun component4(): Relation<T> = invoke()
  operator fun component5(): Relation<T> = invoke()
  operator fun component6(): Relation<T> = invoke()
  operator fun component7(): Relation<T> = invoke()
  operator fun component8(): Relation<T> = invoke()
  operator fun component9(): Relation<T> = invoke()
}

/** A [Variables] is a helper class which allows to create multiple [Variable]s at once. */
fun interface Variables<out T> {
  operator fun invoke(): Variable<T>
  operator fun component1(): Variable<T> = invoke()
  operator fun component2(): Variable<T> = invoke()
  operator fun component3(): Variable<T> = invoke()
  operator fun component4(): Variable<T> = invoke()
  operator fun component5(): Variable<T> = invoke()
  operator fun component6(): Variable<T> = invoke()
  operator fun component7(): Variable<T> = invoke()
  operator fun component8(): Variable<T> = invoke()
  operator fun component9(): Variable<T> = invoke()
}

/**
 * Runs a Datalog program within the given [scope] and returns the result. The [scope] is a
 * [DatalogScope] receiver.
 *
 * @param R the return type of the Datalog program.
 * @param scope the scope in which the Datalog program is run.
 * @return the result of the Datalog program.
 */
// TODO : Make this inline.
fun <T, R> datalog(scope: DatalogScope<T>.() -> R): R = Datalog<T>().scope()

// IMPLEMENTATION

private class Datalog<T> : DatalogScope<T> {
  private var nextVariableId = 0
  private var nextRelationId = 0
  override fun variable() = Variable<T>(nextVariableId++)
  override fun relation() = Relation<T>(nextRelationId++)
  override fun Term<T>.plusAssign(terms: Terms<T>) {
    // TODO : Implement this.
  }
  override fun solve(relation: Relation<T>): Set<Term<T>> {
    // TODO : Implement this.
    return setOf(Term(relation, listOf(variable(), variable(), variable()), false))
  }
}
