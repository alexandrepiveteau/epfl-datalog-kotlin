package io.github.alexandrepiveteau.datalog.collections.vector

import kotlin.test.Test
import kotlin.test.assertFailsWith

class IntVectorTest {

  @Test
  fun `new vector is empty`() {
    val vector = IntVector()
    assert(vector.size == 0)
  }

  @Test
  fun `get with negative index throws`() {
    val vector = IntVector()
    assertFailsWith<IndexOutOfBoundsException> { vector[-1] }
  }

  @Test
  fun `get with out of bounds index throws`() {
    val vector = IntVector()
    assertFailsWith<IndexOutOfBoundsException> { vector[0] }
  }

  @Test
  fun `set with negative index throws`() {
    val vector = IntVector()
    assertFailsWith<IndexOutOfBoundsException> { vector[-1] = 0 }
  }

  @Test
  fun `set with out of bounds index throws`() {
    val vector = IntVector()
    assertFailsWith<IndexOutOfBoundsException> { vector[0] = 0 }
  }

  @Test
  fun `add increases size`() {
    val vector = IntVector()
    vector.add(0)
    assert(vector.size == 1)
  }

  @Test
  fun `add thousands of elements`() {
    val vector = IntVector()
    repeat(1000) { vector.add(it) }
    assert(vector.size == 1000)
  }
}
