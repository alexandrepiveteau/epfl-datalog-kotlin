package io.github.alexandrepiveteau.datalog.dsl

import io.github.alexandrepiveteau.datalog.core.Algorithm
import io.github.alexandrepiveteau.datalog.core.Predicate
import io.github.alexandrepiveteau.datalog.core.ProgramBuilder as CoreProgramBuilder

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

private class Datalog<T>(domain: Domain<T>, algorithm: Algorithm) : DatalogScope<T> {
  private val builder = CoreProgramBuilder(domain, algorithm)

  // This function is needed because of the cross-references between `translation` and `builder`.

  override fun variable() = builder.variable()
  override fun predicate() = builder.predicate()

  override fun Term<T>.plusAssign(terms: Terms<T>) {
    return builder.rule(predicate = predicate, atoms = atoms) {
      for ((relation, atoms, negated) in terms.terms) {
        predicate(relation, atoms, negated)
      }
    }
  }

  override fun Term<T>.plusAssign(aggregation: Aggregation<T>) {
    return builder.rule(predicate = predicate, atoms = atoms) {
      val (p, a) = aggregation
      predicate(p.predicate, p.atoms, p.negated)
      aggregate(a.aggregate, a.same, a.columns, a.result)
    }
  }

  override fun solve(predicate: Predicate, arity: Int): Set<Term<T>> {
    return builder
        .build()
        .solve(predicate, arity)
        .asSequence()
        .map { Term(predicate = it.predicate, atoms = it.atoms, negated = false) }
        .toSet()
  }
}
