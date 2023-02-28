package io.github.alexandrepiveteau.datalog.collections.vector

/**
 * A class representing a vector of ints. This class avoids boxing the ints, and is thus more
 * efficient than a [List] of [Int].
 */
class IntVector {

  private var backing = IntArray(16)

  /** Returns the number of elements in this [IntVector]. */
  var size: Int = 0
    private set

  /** Returns the element at the given [index] in this [IntVector]. */
  operator fun get(index: Int): Int {
    if (index < 0 || index >= size)
        throw IndexOutOfBoundsException("Index $index is out of bounds for size $size.")
    return backing[index]
  }

  /**
   * Sets the element at the given [index] in this [IntVector] to the given [value].
   *
   * @param index the index of the element to set.
   * @param value the value to set.
   */
  operator fun set(index: Int, value: Int) {
    if (index < 0 || index >= size)
        throw IndexOutOfBoundsException("Index $index is out of bounds for size $size.")
    backing[index] = value
  }

  /** Grow the backing array to the given [size], if necessary. */
  private fun ensureCapacity(size: Int) {
    while (size > backing.size) {
      val newBacking = IntArray(backing.size * 2)
      backing.copyInto(newBacking)
      backing = newBacking
    }
  }

  /**
   * Adds the given [value] to the end of this [IntVector].
   *
   * @param value the value to add.
   */
  fun add(value: Int) {
    ensureCapacity(size + 1)
    backing[size] = value
    size++
  }

  /** Performs the given [action] on each element of this [IntVector]. */
  inline fun forEach(action: (Int) -> Unit) = forEachIndexed { _, it -> action(it) }

  /** Performs the given [action] on each element of this [IntVector]. */
  inline fun forEachIndexed(action: (Int, Int) -> Unit) = repeat(size) { action(it, get(it)) }

  /** Removes all the elements from this [IntVector], leaving it empty. */
  fun clear() {
    size = 0
  }
}
