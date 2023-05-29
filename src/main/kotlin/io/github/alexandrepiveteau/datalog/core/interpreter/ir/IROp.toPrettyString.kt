package io.github.alexandrepiveteau.datalog.core.interpreter.ir

import io.github.alexandrepiveteau.datalog.core.interpreter.algebra.Column

/**
 * An interface representing a [Diagram], which is a textual representation of of text. [Diagram]s
 * have a fixed [width], meaning that all their lines will have the same length. They also have a
 * [height], which is the number of lines in the diagram.
 */
private interface Diagram {

  /** The length of each line. This does not include the trailing newline. */
  val width: Int

  /** The number of lines in the diagram. */
  val height: Int

  /** Returns the line at the given [index]. The index must be in the range of [0, height). */
  fun line(index: Int): String

  /** Renders this [Diagram] as a [String]. */
  fun render(): String = buildString {
    for (i in 0 until height) {
      append(line(i))
      append('\n')
    }
  }

  companion object
}

/** Returns a [Diagram] for a [String] input. */
private data class StringDiagram(private val text: String) : Diagram {
  private val lines = text.lines()
  private val longestLine = lines.maxOfOrNull { it.length } ?: 0
  override val width = longestLine
  override val height = lines.size
  override fun line(index: Int): String {
    return lines.getOrNull(index)?.padEnd(longestLine) ?: ""
  }
}

/** Returns a [Diagram] from a [String]. */
private fun Diagram.Companion.from(text: String): Diagram = StringDiagram(text)

/** Wraps this [Diagram] in a box. */
private data class Boxed(private val diagram: Diagram) : Diagram {
  override val width
    get() = diagram.width + 2
  override val height
    get() = diagram.height + 2
  override fun line(index: Int): String {
    return when (index) {
      0 -> "┌${"─".repeat(diagram.width)}┐"
      height - 1 -> "└${"─".repeat(diagram.width)}┘"
      else -> "│${diagram.line(index - 1)}│"
    }
  }
}

/** Boxes this [Diagram]. */
private fun Diagram.boxed(): Diagram = Boxed(this)

/** Wraps this [Diagram] in parentheses. */
private data class Parenthesised(private val diagram: Diagram) : Diagram {
  override val width: Int
    get() = diagram.width + 4
  override val height: Int
    get() = diagram.height
  override fun line(index: Int): String {
    return when (index) {
      0 -> "┌ ${diagram.line(index)} ┐"
      height - 1 -> "└ ${diagram.line(index)} ┘"
      else -> "│ ${diagram.line(index)} │"
    }
  }
}

/** Parenthesizes this [Diagram]. */
private fun Diagram.parenthesised(): Diagram = Parenthesised(this)

/** Returns a [Diagram] for a [List] of [Diagram]s, which will be horizontally aligned. */
private data class HorizontallyAligned(private val diagrams: List<Diagram>) : Diagram {
  override val width: Int
    get() = diagrams.sumOf { it.width }
  override val height: Int
    get() = diagrams.maxOfOrNull { it.height } ?: 0
  override fun line(index: Int): String {
    return diagrams.joinToString("") { diagram -> diagram.line(index).padEnd(diagram.width) }
  }
}

/** Horizontally aligns a [List] of [Diagram]s. */
private fun Diagram.Companion.horizontallyAligned(diagrams: List<Diagram>): Diagram =
    HorizontallyAligned(diagrams)

/** Returns a [Diagram] for a [Diagram], which will be vertically aligned. */
private data class VerticallyCentered(
    private val diagram: Diagram,
    override val height: Int,
) : Diagram {
  override val width: Int
    get() = diagram.width
  override fun line(index: Int): String {
    val offset = (height - diagram.height) / 2
    return if (index in offset until offset + diagram.height) {
      diagram.line(index - offset)
    } else {
      " ".repeat(width)
    }
  }
}

/** Vertically centers this [Diagram] in a [height] of lines. */
private fun Diagram.verticallyCentered(height: Int): Diagram = VerticallyCentered(this, height)

/** Returns a [Diagram] for a [Diagram], which will be vertically aligned. */
private data class VerticallyBottomAligned(
    private val diagram: Diagram,
    override val height: Int,
) : Diagram {
  override val width: Int
    get() = diagram.width

  override fun line(index: Int): String {
    val offset = height - diagram.height
    return if (index in offset until offset + diagram.height) {
      diagram.line(index - offset)
    } else {
      " ".repeat(width)
    }
  }
}

/** Vertically bottom aligns this [Diagram] in a [height] of lines. */
private fun Diagram.verticallyBottomAligned(
    height: Int,
): Diagram = VerticallyBottomAligned(this, height)

// IROp -> Diagram

/** Returns the string representation of the [Column]. */
private fun <T> Column<T>.toPrettyString(): String {
  return when (this) {
    is Column.Constant -> value.value.toString()
    is Column.Index -> "\$${index}"
  }
}

/** Returns a [Diagram] for this [IROp.RelationalIROp]. */
private fun <T> IROp.RelationalIROp<T>.toDiagram(): Diagram {
  return when (this) {
    is IROp.RelationalIROp.Empty -> {
      val content = Array(arity) { "_" }.joinToString(",")
      Diagram.from("Empty(${content})").boxed()
    }
    is IROp.RelationalIROp.Join -> {
      val diagrams = relations.map { it.toDiagram() }
      val join = Diagram.from("⨯")
      val joined = buildList {
        if (diagrams.isNotEmpty()) {
          add(diagrams.first())
          for (i in 1 until diagrams.size) {
            add(join)
            add(diagrams[i])
          }
        }
      }
      val maxHeight = joined.maxOfOrNull { it.height } ?: 0
      Diagram.horizontallyAligned(joined.map { it.verticallyCentered(maxHeight) }).parenthesised()
    }
    is IROp.RelationalIROp.Scan -> {
      val content = Array(arity) { "_" }.joinToString(",")
      val name = predicate.predicate
      Diagram.from("$name[db=$database](${content})").boxed()
    }
    is IROp.RelationalIROp.Select -> {
      val clauses = selection.joinToString(",") { c -> c.joinToString("=") { it.toPrettyString() } }
      val projection = Diagram.from("σ($clauses)")
      val predicate = relation.toDiagram()
      val maxHeight = maxOf(projection.height, predicate.height)
      val aligned = listOf(projection, predicate).map { it.verticallyBottomAligned(maxHeight) }
      Diagram.horizontallyAligned(aligned).parenthesised()
    }
    else -> Diagram.from("Not Implemented")
  }
}

// IROp -> String

/** Returns the [String] representation of this [IROp]. */
internal fun <T> IROp.RelationalIROp<T>.toPrettyString(): String = toDiagram().render()
