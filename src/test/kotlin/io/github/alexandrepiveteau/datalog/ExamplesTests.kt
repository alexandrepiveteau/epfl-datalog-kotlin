package io.github.alexandrepiveteau.datalog

import io.github.alexandrepiveteau.datalog.core.Algorithm
import io.github.alexandrepiveteau.datalog.core.Domain
import io.github.alexandrepiveteau.datalog.core.ProgramBuilder
import io.github.alexandrepiveteau.datalog.core.rule.Fact
import io.github.alexandrepiveteau.datalog.core.rule.Predicate
import io.github.alexandrepiveteau.datalog.core.rule.Rule
import io.github.alexandrepiveteau.datalog.core.rule.Value
import io.github.alexandrepiveteau.datalog.dsl.domains.domain
import io.github.alexandrepiveteau.datalog.parser.DatalogParser
import io.github.alexandrepiveteau.datalog.parser.core.*
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

// File and folder names.
private const val ExamplesFolder = "examples"
private const val CasesFolder = "cases"
private const val InputFolder = "input"
private const val OutputFolder = "output"
private const val ProgramFile = "program.dl"
private const val ConfigFile = "config.dl"

/** Returns a [Sequence] of all the folders in the `examples` folder. */
private fun examples(): Sequence<File> {
  return sequence {
    val classLoader = Thread.currentThread().contextClassLoader
    val path = classLoader.getResource(ExamplesFolder)?.path ?: return@sequence
    val files = File(path).listFiles() ?: return@sequence
    yieldAll(files.asSequence())
  }
}

/** Returns all the cases in the given [folder]. */
private fun cases(folder: File): Sequence<File> {
  return sequence {
    val files = File(folder, CasesFolder).listFiles() ?: return@sequence
    yieldAll(files.asSequence())
  }
}

/** Runs a test case. */
private fun testCase(program: File, case: File) {
  when (val type = File(case, ConfigFile).readText().trim()) {
    "Int" -> testCase(Int.parser(), Int.domain(), program, case)
    "String" -> testCase(all(), String.domain(), program, case)
    "String+Quotes" -> testCase(String.parserQuoted(), String.domain(), program, case)
    else -> fail("Unknown type: $type.")
  }
}

/** Runs a test case of type [T]. */
private fun <T : Any> testCase(constants: Parser<T>, domain: Domain<T>, program: File, case: File) {
  val inputs = File(case, InputFolder).listFiles() ?: fail("No input folder in $case")
  val outputs = File(case, OutputFolder).listFiles() ?: fail("No output folder in $case")
  val programFile = File(program, ProgramFile)

  val rules = rules(constants, programFile)

  for (algorithm in Algorithm.values()) {
    // 1. Prepare the program.
    val builder = ProgramBuilder(domain, algorithm)
    for (rule in rules) builder.rule(rule)
    for (file in inputs) {
      val relation = file.name.split(".").first()
      val predicate = Predicate(relation)
      facts(constants, file).forEach { builder.fact(predicate, it) }
    }

    // 2. Run the program for each tested relation.
    val datalog = builder.build()
    for (file in outputs) {
      val (relation, arity) = file.name.split(".")
      val predicate = Predicate(relation)
      val facts = datalog.solve(predicate, arity.toInt()).toSet()
      val expected = facts(constants, file).toSet()
      facts shouldBe expected
    }
  }
}

/** Returns all the [Rule]s from a file. */
private fun <T : Any> rules(constants: Parser<T>, program: File): List<Rule<T>> {
  val ruleParser = DatalogParser(constants)
  val ws = regexToken(Regex("\\s*"))
  return (separated(ruleParser, ws) and -end()).parse(program.readText()) ?: fail("Bad program.")
}

/** Returns all the [Fact]s from a file. */
private fun <T : Any> facts(constants: Parser<T>, file: File): List<Fact<T>> {
  val lines = file.readLines()
  return lines.map { it.split(",").map { v -> Value(constants.parse(v) ?: fail("Bad value.")) } }
}
