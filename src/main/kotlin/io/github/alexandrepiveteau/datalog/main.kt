package io.github.alexandrepiveteau.datalog

import io.github.alexandrepiveteau.datalog.dsl.Term
import io.github.alexandrepiveteau.datalog.dsl.Value
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
        val (r, v, t, tc) = predicates()
        val (x, y, z) = variables()

        // Set up the EDB
        val max = 40
        for (i in 1..max) {
          r(i, i + 1) += empty
        }
        r(max + 1, 1) += empty

        // P1
        v(x) += r(x, y)
        v(y) += r(x, y)
        // P2
        t(x, y) += r(x, y)
        t(x, y) += t(x, z) + r(z, y)
        // P3
        tc(x, y) += v(x) + v(y) + !t(x, y)

        solve(tc)
      }

  solution
      .map { term -> term.atoms }
      .map { (f, t) -> f as Value<Int> to t as Value<Int> }
      .forEach { (f, t) -> println("${f.value} -> ${t.value}") }
}
