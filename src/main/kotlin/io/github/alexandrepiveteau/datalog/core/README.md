# Datalog engine

This package contains the core Datalog implementation.
It is a pure Kotlin implementation of a solver for a limited subset of Datalog.

## Restrictions

This implementation of Datalog is restricted in the following ways:

+ The only supported data type is opaque, and is represented by the `Atom` type.
+ All values must be pre-declared in the program before they can be used in rules.
+ Negation is only allowed in stratified programs.

The restrictions above are not inherent to Datalog, and are only present in this implementation because they allow for
the following properties:

+ Constants and variables are represented by a single type, which is backed by a primitive value.
+ The domain of constants is known at runtime, and can be used to implement negation.
+ Ranges of constants can be added to the program in constant time.
