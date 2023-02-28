package io.github.alexandrepiveteau.datalog.collections.graph

/**
 * A class representing a directed edge in a [Graph]. Each edge is a pair of vertices, where the
 * first vertex is the source, and the second vertex is the target.
 */
@JvmInline
value class Edge private constructor(private val encoded: Long) {

  /** Constructs a new [Edge] from the given [from] and [to] vertices. */
  constructor(
      from: Vertex,
      to: Vertex,
  ) : this(from.id.toLong() shl 32 or to.id.toLong())

  /** Returns the source vertex of this edge. */
  val from: Vertex
    get() = Vertex((encoded ushr 32).toInt())

  /** Returns the target vertex of this edge. */
  val to: Vertex
    get() = Vertex(encoded.toInt())

  operator fun component1(): Vertex = from
  operator fun component2(): Vertex = to
}
