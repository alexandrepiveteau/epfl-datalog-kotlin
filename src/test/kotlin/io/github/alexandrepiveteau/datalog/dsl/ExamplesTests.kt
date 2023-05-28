package io.github.alexandrepiveteau.datalog.dsl

import io.github.alexandrepiveteau.datalog.core.Algorithm
import io.github.alexandrepiveteau.datalog.core.ProgramBuilder
import io.github.alexandrepiveteau.datalog.core.rule.Fact
import io.github.alexandrepiveteau.datalog.core.rule.Predicate
import io.github.alexandrepiveteau.datalog.core.rule.Value
import io.github.alexandrepiveteau.datalog.dsl.domains.domain
import io.github.alexandrepiveteau.datalog.parser.DatalogParser
import io.github.alexandrepiveteau.datalog.parser.core.parse
import io.github.alexandrepiveteau.datalog.parser.core.parser
import io.kotest.assertions.fail
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.io.File

/**
 * A [StringSpec] that will run all the examples in the `examples` folder. Each example is a folder
 * containing a `program.dl` file, some `input` relations, and some `output` relations.
 */
class ExamplesTests : StringSpec() {
  init {
    for (example in examples()) {
      for (case in cases(example)) {
        "${example.name}/${case.name}" { testCase(example, case) }
      }
    }
  }
}

/** Returns a [Sequence] of all the folders in the `examples` folder. */
private fun examples(): Sequence<File> {
  return sequence {
    val classLoader = Thread.currentThread().contextClassLoader
    val path = classLoader.getResource("examples")?.path ?: return@sequence
    val files = File(path).listFiles() ?: return@sequence
    yieldAll(files.asSequence())
  }
}

/** Returns all the cases in the given [folder]. */
private fun cases(folder: File): Sequence<File> {
  return sequence {
    val files = File(folder, "cases").listFiles() ?: return@sequence
    yieldAll(files.asSequence())
  }
}

/** Returns all the [Fact]s from a file. */
private fun facts(file: File): List<Fact<Int>> {
  val lines = file.readLines()
  return lines.map { it.split(",").map { n -> Value(n.toInt()) } }
}

/** Runs a test case. */
private fun testCase(program: File, case: File) {
  val inputs = File(case, "input").listFiles() ?: fail("No input folder in $case")
  val outputs = File(case, "output").listFiles() ?: fail("No output folder in $case")
  val programFile = File(program, "program.dl")

  val parser = DatalogParser(Int.parser())
  val rules = programFile.readLines().map { parser.parse(it) ?: fail("Bad rule: $it.") }

  for (algorithm in Algorithm.values()) {
    // 1. Prepare the program.
    val builder = ProgramBuilder(Int.domain(), algorithm)
    for (rule in rules) builder.rule(rule)
    for (file in inputs) {
      val relation = file.name.split(".").first()
      val predicate = Predicate(relation)
      facts(file).forEach { builder.fact(predicate, it) }
    }

    // 2. Run the program for each tested relation.
    val datalog = builder.build()
    for (file in outputs) {
      val (relation, arity) = file.name.split(".")
      val predicate = Predicate(relation)
      val facts = datalog.solve(predicate, arity.toInt()).toSet()
      val expected = facts(file).toSet()
      facts shouldBe expected
    }
  }
}
