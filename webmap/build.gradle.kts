plugins {
  id("java")
  alias(libs.plugins.node)
}

node {
  version = "24.19.0"
  download = true
}

tasks {
  clean {
    delete("$projectDir/dist")
  }

  val buildWebmap = register<com.github.gradle.node.npm.task.NpxTask>("buildWebmap") {
    description = "Builds webmap module"
    dependsOn(npmInstall)
    command = "webpack"

    inputs.files(
      listOf(
        "package.json",
        "package-lock.json",
        "tsconfig.json",
        "webpack.config.js",
      )
    )
    inputs.dir("src")
    inputs.dir("public")
    outputs.dir("dist")
  }

  processResources {
    dependsOn(buildWebmap)

    doLast {
      val destinationDir = outputs.files.singleFile.resolve("web")
      outputs.files.singleFile.resolve("dist").renameTo(destinationDir)
      val file = destinationDir.resolve("index.html")
      file.writeText(file.readText().replace($$"${description}", "${rootProject.description}"))
    }
  }
}

sourceSets {
  main {
    java {
      resources {
        srcDir("$projectDir")
        include("dist/**")
      }
    }
  }
}
