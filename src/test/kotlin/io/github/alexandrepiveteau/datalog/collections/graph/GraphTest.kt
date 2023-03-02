package io.github.alexandrepiveteau.datalog.collections.graph

import kotlin.test.*

class GraphTest {

  @Test
  fun `new graph is empty`() {
    val graph = Graph()
    assert(graph.size == 0)
  }

  @Test
  fun `new singleton graph has one vertex`() {
    val graph = Graph { addVertex() }
    assertEquals(1, graph.size)
    assertEquals(0, graph.neighborsSize(0))
  }

  @Test
  fun `duplicate edge insertion is de-duplicated`() {
    val graph = Graph {
      val (a, b) = addVertices()
      addEdge(a, b)
      addEdge(a, b)
    }
    assertEquals(2, graph.size)
    assertEquals(1, graph.neighborsSize(0))
    assertEquals(0, graph.neighborsSize(1))
  }

  @Test
  fun `get with negative index throws`() {
    val graph = Graph()
    assertFailsWith<IndexOutOfBoundsException> { graph[-1] }
  }

  @Test
  fun `dfs with singleton`() {
    val graph = Graph { addVertex() }
    val visited = mutableListOf<Vertex>()
    graph.forEachVertexDFS(graph[0]) { visited.add(it) }
    assertEquals(1, visited.size)
    assertEquals(0, graph[visited[0]])
  }

  @Test
  fun `dfs with cycle of two`() {
    val graph = Graph {
      val (a, b) = addVertices()
      addEdge(a, b)
      addEdge(b, a)
    }
    val visited = mutableListOf<Vertex>()
    graph.forEachVertexDFS(graph[0]) { visited.add(it) }
    assertEquals(2, visited.size)
    assertEquals(0, graph[visited[0]])
    assertEquals(1, graph[visited[1]])
  }

  @Test
  fun `dfs with unreachable node`() {
    val graph = Graph { repeat(2) { addVertex() } }
    val visited = mutableListOf<Vertex>()
    graph.forEachVertexDFS(graph[0]) { visited.add(it) }
    assertEquals(1, visited.size)
    assertEquals(0, graph[visited[0]])
  }

  @Test
  fun `copy returns a new graph`() {
    val graph = Graph()
    val copy = graph.copy()
    assert(graph !== copy)
  }

  @Test
  fun `dfs postorder is correct`() {
    val graph = Graph {
      // 0 -> {2, 3}
      // 1 -> {0}
      // 2 -> {1}
      // 3 -> {4}
      // 4 -> { }
      val (zero, one, two, three, four) = addVertices()

      addEdge(zero, two)
      addEdge(zero, three)
      addEdge(one, zero)
      addEdge(two, one)
      addEdge(three, four)
    }

    val expectedAction = intArrayOf(0, 3, 4, 2, 1)
    val expectedTreated = intArrayOf(4, 3, 1, 2, 0)
    val action = mutableListOf<Vertex>()
    val treated = mutableListOf<Vertex>()

    graph.forEachVertexDFS(
        from = graph[0],
        action = { action.add(it) },
        treated = { treated.add(it) },
    )

    assertContentEquals(expectedAction, action.map { graph[it] }.toIntArray())
    assertContentEquals(expectedTreated, treated.map { graph[it] }.toIntArray())
  }

  @Test
  fun `scc with zero nodes is correct`() {
    val graph = Graph()
    val scc = graph.scc()
    assertEquals(0, scc.size)
  }

  @Test
  fun `scc with one node is correct`() {
    val graph = Graph { addVertex() }
    val scc = graph.scc()

    assertEquals(1, scc.size)
    assertEquals(graph[0], scc[graph[0]])
  }

  @Test
  fun `scc with many distinct nodes is correct`() {
    val count = 100
    val graph = Graph { repeat(count) { addVertex() } }
    val scc = graph.scc()

    assertEquals(count, scc.size)
    repeat(count) { assertEquals(graph[it], scc[graph[it]]) }
  }

  @Test
  fun `scc with path has one component per node`() {
    val graph = Graph {
      val (a, b, c) = addVertices()

      addEdge(a, b)
      addEdge(b, c)
    }

    val scc = graph.scc()
    assertEquals(3, scc.size)
  }

  @Test
  fun `scc with two nodes cycle has one component`() {
    val graph = Graph {
      val (a, b) = addVertices()

      addEdge(a, b)
      addEdge(b, a)
    }

    val scc = graph.scc()
    assertEquals(1, scc.values.toSet().size)
    assertEquals(scc[graph[1]], scc[graph[0]])
  }

  @Test
  fun `scc with three nodes cycle has one component`() {
    val graph = Graph {
      val (a, b, c) = addVertices()
      addEdge(a, b)
      addEdge(b, c)
      addEdge(c, a)
    }

    val scc = graph.scc()
    assertEquals(1, scc.values.toSet().size)
    assertEquals(scc[graph[1]], scc[graph[0]])
    assertEquals(scc[graph[2]], scc[graph[0]])
  }

  @Test
  fun `scc with two cycles has two components`() {
    val graph = Graph {
      val (a, b, c, d) = addVertices()
      addEdge(a, b)
      addEdge(b, a)

      addEdge(c, d)
      addEdge(d, c)
    }

    val scc = graph.scc()
    assertEquals(2, scc.values.toSet().size)
    assertEquals(scc[graph[1]], scc[graph[0]])
    assertEquals(scc[graph[3]], scc[graph[2]])
    assertNotEquals(scc[graph[1]], scc[graph[2]])
  }
}
