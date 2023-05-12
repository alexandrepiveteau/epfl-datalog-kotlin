plugins { kotlin("jvm") version "1.7.21" }

repositories { mavenCentral() }

dependencies {
  implementation(kotlin("stdlib"))
  testImplementation(kotlin("test"))
  implementation("io.github.alexandrepiveteau:kotlin-graphs:0.5.0")
}
