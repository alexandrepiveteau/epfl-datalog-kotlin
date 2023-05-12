# epfl-datalog-kotlin

Datalog evaluation engine for Kotlin.

```kotlin
datalog<Int, Set<Term<Int>>> {
  val (r, v, t, tc) = predicates()
  val (x, y, z) = variables()

  // Set up the EDB
  r(1, 2) += empty
  r(2, 3) += empty
  r(3, 4) += empty
  r(4, 1) += empty

  // P1
  v(x) += r(x, y)
  v(y) += r(x, y)
  // P2
  t(x, y) += r(x, y)
  t(x, y) += t(x, z) + r(z, y)
  // P3
  tc(x, y) += v(x) + v(y) + !t(x, y)

  solve(tc) // Yields an empty set.
}

```

## Download

Coming soon !

## Features

+ Written in uncomplicated Kotlin
+ Supports various datalog features
    - Naive and semi-naive evaluation
    - Stratified negation
+ Designed with both a high-level and low-level API
+ Works on Kotlin/JVM. Kotlin/JS and Kotlin/Native coming soon !

## Usage

Coming soon !
