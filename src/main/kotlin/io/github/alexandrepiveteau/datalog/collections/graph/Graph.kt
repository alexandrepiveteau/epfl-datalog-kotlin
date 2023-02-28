package io.github.alexandrepiveteau.datalog.collections.graph

/**
 * A [Graph] is a data structure which represents a set of vertices, and a set of edges between
 * vertices. The edges are directed, and the graph is immutable.
 *
 * Vertices and edges are indexed, and can be accessed using the [get] operator. The [size] of the
 * graph is the number of vertices in the graph, and the [neighborsSize] of a vertex is the number
 * of edges leaving that vertex.
 */
interface Graph {

  /** Returns the number of vertices in this graph. */
  val size: Int

  /** Returns the index for the given [Vertex] in this graph. */
  operator fun get(vertex: Vertex): Int

  /** Returns the [Vertex] at the given [index] in this graph. */
  operator fun get(index: Int): Vertex

  /** Returns the number of edges leaving the given [Vertex] in this graph. */
  fun neighborsSize(index: Int): Int

  /**
   * Returns the [Vertex] at the given [index] in the list of neighbors of the given [vertex].
   *
   * @param vertex the vertex whose neighbors are being accessed.
   * @param index the index of the neighbor to access.
   * @return the neighbor at the given index.
   */
  fun neighbor(vertex: Vertex, index: Int): Vertex

  /** @see neighbor */
  operator fun get(vertex: Vertex, index: Int): Vertex = neighbor(vertex, index)
}

/**
 * Executes the given [action] on each vertex in this graph.
 *
 * @param action the action to execute on each vertex.
 * @receiver the graph on which to execute the action.
 */
inline fun Graph.forEachVertex(action: (Vertex) -> Unit) {
  for (i in 0 until size) {
    action(get(i))
  }
}

/**
 * Executes the given [action] on each neighbor of the given [vertex].
 *
 * @param vertex the vertex whose neighbors are being accessed.
 * @param action the action to execute on each neighbor.
 * @receiver the graph on which to execute the action.
 */
inline fun Graph.forEachNeighbor(vertex: Vertex, action: (Vertex) -> Unit) {
  val index = get(vertex)
  for (i in 0 until neighborsSize(index)) {
    action(neighbor(vertex, i))
  }
}

/**
 * Executes the given [action] on each edge in this graph.
 *
 * @param action the action to execute on each edge.
 * @receiver the graph on which to execute the action.
 */
inline fun Graph.forEachEdge(action: (Edge) -> Unit) {
  forEachVertex { from -> forEachNeighbor(from) { to -> action(Edge(from, to)) } }
}

/**
 * Traverses the [Graph] using a depth-first search algorithm, starting from the given [from]
 * vertex. The [action] is executed on each vertex in the order they are visited.
 *
 * @param from the vertex from which to start the search.
 * @param treated the action to execute on each vertex after all its neighbors have been visited.
 * @param action the action to execute on each vertex.
 */
fun Graph.forEachVertexDFS(
    from: Vertex,
    treated: (Vertex) -> Unit = {},
    action: (Vertex) -> Unit = {},
) {
  val visited = BooleanArray(size)
  val queue = ArrayDeque<Vertex>() // TODO : Avoid auto-boxing with a custom queue.
  queue.add(from)
  while (queue.isNotEmpty()) {
    val vertex = queue.last()
    val index = get(vertex)
    if (!visited[index]) {
      visited[index] = true
      action(vertex)
      forEachNeighbor(vertex) { if (!visited[get(it)]) queue.add(it) }
    } else if (visited[index]) {
      treated(vertex)
      queue.removeLast()
    }
  }
}
