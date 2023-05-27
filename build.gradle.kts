plugins { kotlin("jvm") version "1.8.20" }

repositories { mavenCentral() }

dependencies {
  implementation(kotlin("stdlib"))
  implementation("io.github.alexandrepiveteau:kotlin-graphs:0.5.0")

  testImplementation("io.kotest:kotest-assertions-core:5.6.2")
  testImplementation("io.kotest:kotest-framework-datatest:5.6.2")
  testImplementation("io.kotest:kotest-property:5.6.2")
  testImplementation("io.kotest:kotest-runner-junit5:5.6.2")
}

tasks.withType<Test>().configureEach { useJUnitPlatform() }
