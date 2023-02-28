package io.github.alexandrepiveteau.datalog.collections.graph

/**
 * A class representing a vertex in a [Graph]. Each vertex is identified with a unique identifier
 * within the graph.
 *
 * @param id the identifier of the vertex.
 */
@JvmInline value class Vertex internal constructor(internal val id: Int)
