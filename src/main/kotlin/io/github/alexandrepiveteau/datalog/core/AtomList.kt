package io.github.alexandrepiveteau.datalog.core

/** An interface representing a list of [Atom]. */
interface AtomList {

  /** The number of atoms in the list. */
  val size: Int

  /**
   * Returns the number of atoms at the given [index], or throws an [IndexOutOfBoundsException] if
   * the index is out of bounds of the list.
   */
  operator fun get(index: Int): Atom
}

inline fun AtomList.forEach(f: (Atom) -> Unit) {
  repeat(size) { f(this[it]) }
}

inline fun AtomList.forEachIndexed(f: (Int, Atom) -> Unit) {
  repeat(size) { f(it, this[it]) }
}

operator fun AtomList.plus(other: AtomList): AtomList = (map { it } + other.map { it }).asAtomList()

inline fun <T> AtomList.map(f: (Atom) -> T): List<T> = buildList { this@map.forEach { add(f(it)) } }

fun AtomList.toList(): List<Atom> = map { it }

fun Array<Atom>.asAtomList(): AtomList = toList().asAtomList()

fun List<Atom>.asAtomList(): AtomList {
  return object : AtomList {
    override val size: Int
      get() = this@asAtomList.size

    override fun get(index: Int): Atom = this@asAtomList[index]

    override fun toString(): String {
      return joinToString()
    }
  }
}
