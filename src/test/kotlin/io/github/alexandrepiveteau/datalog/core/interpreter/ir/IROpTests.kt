package io.github.alexandrepiveteau.datalog.core.interpreter.ir

import io.github.alexandrepiveteau.datalog.core.interpreter.algebra.Column
import io.github.alexandrepiveteau.datalog.core.interpreter.database.PredicateWithArity
import io.github.alexandrepiveteau.datalog.core.interpreter.ir.IROp.RelationalIROp.*
import io.github.alexandrepiveteau.datalog.core.rule.Predicate
import io.github.alexandrepiveteau.datalog.core.rule.Value
import io.kotest.core.spec.style.StringSpec

class IROpTests : StringSpec() {
  init {
    "test pretty string" {
      val empty = Empty(2)
      val scan = Scan<Int>(Database.Base, PredicateWithArity(Predicate("e"), 3))
      println("---")
      println()
      println(empty.toPrettyString())
      println()
      println(scan.toPrettyString())
      println()
      println(
          Join(
                  listOf(
                      empty,
                      Select(
                          selection =
                              setOf(
                                  setOf(Column.Index(3), Column.Index(4)),
                                  setOf(
                                      Column.Index(0), Column.Index(2), Column.Constant(Value(1)))),
                          relation = Join(listOf(empty, scan)))))
              .toPrettyString())
      println()
      println("---")
    }
  }
}
