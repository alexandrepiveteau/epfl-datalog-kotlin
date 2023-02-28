package io.github.alexandrepiveteau.datalog

import io.github.alexandrepiveteau.datalog.dsl.Term
import io.github.alexandrepiveteau.datalog.dsl.datalog

/** Returns a [Sequence] of the standard input text. */
private fun input() = sequence {
  while (true) {
    val line = readlnOrNull() ?: break
    if (line.isEmpty()) break
    yield(line)
    yield("\n")
  }
}

/** The main entry point for the application. */
fun main() {
  val solution =
      datalog<Int, Set<Term<Int>>> {
        val (r, v, t, tc) = relations()
        val (x, y, z) = variables()

        // P1
        v(x) += r(x, y)
        v(y) += r(x, y)
        // P2
        t(x, y) += r(x, y)
        t(x, y) += t(x, z) + r(z, y)
        // P3
        tc(x, y) += v(x) + v(y) + !t(x, y)

        // Set up the EDB
        solve(tc)
      }

  println(solution)
}
