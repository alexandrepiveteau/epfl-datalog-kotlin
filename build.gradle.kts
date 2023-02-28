plugins {
  application
  kotlin("jvm") version "1.7.21"
}

repositories { mavenCentral() }

dependencies {
  implementation(kotlin("stdlib"))
  testImplementation(kotlin("test"))
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
}
