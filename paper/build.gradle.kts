plugins {
  alias(libs.plugins.paperweight)
  alias(libs.plugins.shadow)
}

dependencies {
  compileOnly(project(":core"))

  implementation(libs.bstats)

  paperweight.paperDevBundle(libs.versions.paper)
}

tasks {
  compileJava {
    dependsOn(":core:shadowJar")
  }

  build {
    dependsOn(shadowJar)
  }

  shadowJar {
    archiveClassifier.set("")

    arrayOf(
      "org.bstats"
    ).forEach { relocate(it, "net.pl3x.$it") }
  }
}
