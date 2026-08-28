plugins {
  checkstyle
  alias(libs.plugins.fix.javadoc)
  alias(libs.plugins.shadow)
}

java {
  withJavadocJar()
  withSourcesJar()
}

checkstyle {
  toolVersion = "14.0.0"

  // Explicitly point to the root project's layout so 'core' can find the ruleset
  configDirectory.set(rootProject.file("config/checkstyle"))
  configFile = rootProject.file("config/checkstyle/checkstyle.xml")

  configProperties = mapOf(
    "rootDir" to rootProject.rootDir
  )
}

dependencies {
  // include into livemap jar
  implementation(libs.bstats)
  implementation(libs.bundles.adventure)
  implementation(libs.caffeine)
  implementation(libs.bluenbt)
  implementation(libs.undertow)

  // included by paper module
  compileOnly(libs.simpleYaml)

  // will be provided at runtime
  compileOnly(libs.annotations)
  compileOnly(libs.apache)
  compileOnly(libs.brigadier)
  compileOnly(libs.fastutil)
  compileOnly(libs.gson)
  compileOnly(libs.guava)
  compileOnly(libs.log4j)
  compileOnly(libs.lz4)
}

tasks {
  build {
    dependsOn(shadowJar)
  }

  shadowJar {
    mergeServiceFiles()
    archiveClassifier.set("")

    // shuts up a weird undertow warning in build output
    duplicatesStrategy = DuplicatesStrategy.INCLUDE

    exclude(
      "META-INF/LICENSE*",
      "META-INF/maven/**/*",
      "META-INF/versions/**/*",
      "META-INF/*.idx",
      "schema/**/*"
    )

    arrayOf(
      "com.github.benmanes.caffeine",
      "com.google.errorprone.annotations",
      "de.bluecolored.bluenbt",
      "io.smallrye",
      "io.undertow",
      "net.kyori.adventure",
      "org.bstats",
      "org.checkerframework",
      "org.jboss",
      "org.jspecify",
      "org.simpleyaml",
      "org.wildfly",
      "org.xnio",
    ).forEach { relocate(it, "${rootProject.group}.libs.$it") }
  }

  checkstyleTest {
    isEnabled = false
  }

  javadoc {
    val name = rootProject.name.replaceFirstChar { it.uppercase() }
    val stdopts = options as StandardJavadocDocletOptions
    stdopts.encoding = Charsets.UTF_8.name()
    stdopts.overview = "src/main/javadoc/overview.html"
    stdopts.use()
    stdopts.isDocFilesSubDirs = true
    stdopts.windowTitle = "$name ${rootProject.version} API Documentation"
    stdopts.docTitle = "<h1>$name ${rootProject.version} API</h1>"
    stdopts.header = """<img src="https://raw.githubusercontent.com/billygalbreath/livemap/v4/webmap/public/images/livemap-white.png" style="height:32px">"""
    stdopts.bottom = "Copyright © 2020-2026 William Blake Galbreath"
    stdopts.linkSource(true)
    stdopts.addBooleanOption("html5", true)
    stdopts.addStringOption("Xdoclint:all,-missing", "-quiet") // hides all lint warnings (checkstyle handles reporting better)
    stdopts.addStringOption("-link-modularity-mismatch", "info") // hides named modules warning
    stdopts.links(
      "https://docs.oracle.com/en/java/javase/25/docs/api/", // doesnt seem to do anything
      "https://javadoc.io/doc/org.jetbrains/annotations/${libs.versions.annotations.get()}/",
      "https://javadoc.io/doc/org.apache.commons/commons-lang3/${libs.versions.apache.get()}/",
      "https://docs.pl3x.net/bluenbt/",
      "https://docs.pl3x.net/brigadier",
      "https://javadoc.io/doc/org.bstats/bstats-bukkit/${libs.versions.bstats.get()}/",
      "https://javadoc.io/doc/com.github.ben-manes.caffeine/caffeine/${libs.versions.caffeine.get()}/", // named modules
      "https://javadoc.io/doc/it.unimi.dsi/fastutil/${libs.versions.fastutil.get()}/",
      "https://javadoc.io/doc/com.google.code.gson/gson/${libs.versions.gson.get()}/", // named modules
      "https://javadoc.io/doc/com.google.guava/guava/${libs.versions.guava.get()}/",
      "https://logging.apache.org/log4j/2.x/javadoc/log4j-core/",
      "https://javadoc.io/doc/at.yawk.lz4/lz4-java/${libs.versions.lz4.get()}/", // named modules
      "https://javadoc.io/doc/io.undertow/undertow-core/${libs.versions.undertow.get()}/",
      "https://carleslc.me/Simple-YAML/doc/"
    )
  }

  withType<com.jeff_media.fixjavadoc.FixJavadoc> {
    configureEach {
      newLineOnMethodParameters.set(false)
      keepOriginal.set(false)
    }
  }
}
