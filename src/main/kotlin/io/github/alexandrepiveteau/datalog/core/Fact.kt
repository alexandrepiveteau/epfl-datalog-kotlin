package io.github.alexandrepiveteau.datalog.core

/** A [Fact] defines some constant atoms for a [Predicate]. */
data class Fact(val predicate: Predicate, val atoms: AtomList) {
  init {
    atoms.forEach { require(it.isConstant) { "Atoms in a fact must be constants." } }
  }
}
