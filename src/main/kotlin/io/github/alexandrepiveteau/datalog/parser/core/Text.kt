package io.github.alexandrepiveteau.datalog.parser.core

@JvmInline
value class Text private constructor(private val text: String) {
  val length: Int
    get() = text.length
  fun drop(n: Int): Text = Text(text.drop(n))
  fun startsWith(value: Text): Boolean = text.startsWith(value.text)
  fun indexOf(value: Text): Int = text.indexOf(value.text)
  fun substring(from: Int = 0, until: Int = text.length): Text = Text(text.substring(from, until))
  operator fun get(index: Int): Char = text[index]
  companion object {
    fun of(text: String): Text = Text(text)
    fun of(sequence: Sequence<String>): Text = Text(sequence.joinToString(""))
  }
}
