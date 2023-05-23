package io.github.alexandrepiveteau.datalog.core.interpreter.algebra

import io.github.alexandrepiveteau.datalog.core.Atom
import io.github.alexandrepiveteau.datalog.core.Domain

/** An implementation of [Domain] which uses [Int]s. */
internal object IntDomain : Domain {

  operator fun get(value: Int): Atom {
    require(value >= 0) { "The value must be positive." }
    return Atom(value)
  }

  operator fun get(atom: Atom): Int {
    require(atom.isConstant) { "The value must be a constant." }
    return atom.backing
  }

  override fun sum(a: Atom, b: Atom): Atom = get(get(a) + get(b))
  override fun max(a: Atom, b: Atom): Atom = get(maxOf(get(a), get(b)))
  override fun min(a: Atom, b: Atom): Atom = get(minOf(get(a), get(b)))
}
