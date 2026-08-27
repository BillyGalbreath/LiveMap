plugins {
  alias(libs.plugins.paperweight)
  alias(libs.plugins.shadow)
}

dependencies {
  compileOnly(project(":core"))

  implementation(libs.simpleYaml)

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

    exclude(
      "META-INF/maven/**/*"
    )

    arrayOf(
      "org.simpleyaml",
      "org.yaml",
    ).forEach { relocate(it, "${rootProject.group}.libs.$it") }
  }
}
