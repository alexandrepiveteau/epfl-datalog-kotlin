package io.github.alexandrepiveteau.datalog.core

/** A [Fact] defines some constant atoms for a [Relation]. */
data class Fact(val relation: Relation, val atoms: AtomList) {
  init {
    atoms.forEach { require(it.isConstant) { "Atoms in a fact must be constants." } }
  }
}
