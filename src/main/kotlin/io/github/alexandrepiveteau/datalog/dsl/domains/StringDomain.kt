package io.github.alexandrepiveteau.datalog.dsl.domains

import io.github.alexandrepiveteau.datalog.core.Domain
import io.github.alexandrepiveteau.datalog.core.rule.Value

/** An implementation of [Domain] for [String] values. */
private object StringDomain : Domain<String> {
  override fun unit() = throw UnsupportedOperationException()
  override fun min(a: Value<String>, b: Value<String>) = if (a.value < b.value) a else b
  override fun max(a: Value<String>, b: Value<String>) = if (a.value > b.value) a else b
  override fun sum(a: Value<String>, b: Value<String>) = throw UnsupportedOperationException()
}

/** Returns the [Domain] for [String] values. */
fun String.Companion.domain(): Domain<String> = StringDomain
