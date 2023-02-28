package io.github.alexandrepiveteau.datalog.collections.graph

import io.github.alexandrepiveteau.datalog.collections.vector.IntVector

/**
 * A [GraphBuilder] is a mutable data structure which can be used to build a [Graph]. It is
 * implemented as a builder, and can be used to build a graph in a single expression.
 */
interface GraphBuilder {

  /** Adds a new vertex to the graph, and returns it. */
  fun addVertex(): Vertex

  /**
   * Adds a new edge to the graph.
   *
   * @param edge the edge to add.
   */
  fun addEdge(edge: Edge) = addEdge(edge.from, edge.to)

  /**
   * Adds a new edge to the graph.
   *
   * @param from the source vertex of the edge.
   * @param to the target vertex of the edge.
   */
  fun addEdge(from: Vertex, to: Vertex)
}

/** Creates a new [Graph] from the given [builder]. */
fun Graph(
    builder: GraphBuilder.() -> Unit = {},
): Graph = ForwardGraphBuilder().apply(builder).toGraph()

/** Updates this [Graph] using the given [builder]. */
fun Graph.copy(builder: GraphBuilder.() -> Unit = {}) = Graph {
  forEachVertex { addVertex() }
  forEachEdge { addEdge(it) }
  builder()
}

/** Returns a new [Graph] which is the transposed of this [Graph]. */
fun Graph.transposed(): Graph = Graph {
  forEachVertex { addVertex() }
  forEachEdge { addEdge(it.to, it.from) }
}

// Forward star representation implementation.

/** A [GraphBuilder] which builds a [Graph] using a forward star representation. */
private class ForwardGraphBuilder : GraphBuilder {
  private var nextId = 0
  private val edges = IntVector()
  override fun addVertex(): Vertex = Vertex(nextId++)
  override fun addEdge(from: Vertex, to: Vertex) {
    edges.add(from.id)
    edges.add(to.id)
  }

  /** Builds a [Graph] from the current state of this builder. */
  fun toGraph(): Graph {
    // Count the number of outgoing edges for each vertex.
    val present = Array(nextId) { BooleanArray(nextId) }
    for (i in 0 until edges.size step 2) {
      present[edges[i]][edges[i + 1]] = true
    }

    // Create the neighbors array.
    val neighbors = Array(nextId) { i -> IntArray(present[i].count { it }) }

    // Fill the neighbors array, starting from the end of each array.
    val counts = IntArray(nextId)
    present.forEachIndexed { from, arr ->
      arr.forEachIndexed { to, present ->
        if (present) {
          neighbors[from][counts[from]++] = to
        }
      }
    }

    return ForwardGraph(neighbors)
  }
}

/**
 * A [Graph] is a data structure which represents a set of vertices, and a set of edges between
 * vertices. The edges are directed, and the graph is immutable.
 */
private class ForwardGraph(private val neighbors: Array<IntArray>) : Graph {

  override val size: Int
    get() = neighbors.size

  override fun get(vertex: Vertex): Int {
    if (vertex.id < 0 || vertex.id >= size) {
      throw IndexOutOfBoundsException("Vertex $vertex is out of bounds.")
    }
    return vertex.id
  }

  override fun get(index: Int): Vertex {
    if (index < 0 || index >= size) {
      throw IndexOutOfBoundsException("Index $index is out of bounds.")
    }
    return Vertex(index)
  }

  override fun neighborsSize(index: Int): Int {
    if (index < 0 || index >= size) {
      throw IndexOutOfBoundsException("Index $index is out of bounds.")
    }
    return neighbors[index].size
  }

  override fun neighbor(vertex: Vertex, index: Int): Vertex {
    if (vertex.id < 0 || vertex.id >= size) {
      throw IndexOutOfBoundsException("Vertex $vertex is out of bounds.")
    }
    if (index < 0 || index >= neighbors[vertex.id].size) {
      throw IndexOutOfBoundsException("Index $index is out of bounds.")
    }
    return Vertex(neighbors[vertex.id][index])
  }
}
