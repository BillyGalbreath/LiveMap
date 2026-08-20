plugins {
  `java-library`
  `maven-publish`
}

var buildNum = System.getenv("GITHUB_RUN_NUMBER") ?: "SNAPSHOT"
val authors = providers.gradleProperty("authors").get()
val website = providers.gradleProperty("website").get()

group = "net.pl3x.livemap"
version = "${libs.versions.livemap.get()}-$buildNum"
description = providers.gradleProperty("description").get()

allprojects {
  pluginManager.apply("java-library")

  java {
    toolchain.languageVersion = JavaLanguageVersion.of(25)
  }

  tasks {
    compileJava {
      options.encoding = Charsets.UTF_8.name()
      options.release = 25
    }
  }
}

subprojects {
  base {
    archivesName = "${rootProject.name}-${rootProject.version}-${project.name}"
  }

  repositories {
    mavenCentral()
    mavenLocal()
    maven("https://jitpack.io")
    maven("https://libraries.minecraft.net")
    maven("https://repo.bluecolored.de/releases") {
      content { includeGroupByRegex("de\\.bluecolored.*") }
    }
  }

  dependencies {
    compileOnly(rootProject.libs.annotations)

    testImplementation(rootProject.libs.junit.get())
    testImplementation(rootProject.libs.asm.get())
    testRuntimeOnly(rootProject.libs.junitPlatform.get())
  }

  configurations {
    // ensure JUnit tests get the same dependencies as compileOnly
    testImplementation.get().extendsFrom(compileOnly.get())
  }

  tasks {
    test {
      useJUnitPlatform()
      // we want to see system.out from tests
      testLogging.showStandardStreams = true
    }

    processResources {
      filteringCharset = Charsets.UTF_8.name()

      // work around IDEA-296490
      duplicatesStrategy = DuplicatesStrategy.INCLUDE
      with(copySpec {
        include("*plugin.yml", "fabric.mod.json")
        from("src/main/resources") {
          expand(
            "name" to rootProject.name,
            "group" to rootProject.group,
            "version" to rootProject.version,
            "minecraft" to libs.versions.minecraft.get(),
            "description" to "${rootProject.description}",
            "authors" to authors,
            "website" to website
          )
        }
      })
    }
  }
}

// this must be after subprojects block
tasks {
  build {
    dependsOn("copyJavadocAndSources")
  }

  withType<Jar> {
    subprojects {
      dependsOn(project.tasks.build)
    }

    // merge them into main jar (except their manifests)
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(subprojects.map { zipTree(it.tasks.jar.get().archiveFile.get().asFile) }) {
      exclude("META-INF/MANIFEST.MF")
    }
  }
}

tasks.register("copyJavadocAndSources", Copy::class) {
  description = "Copy Javadoc and Sources from Core"
  from(cp("javadoc"), cp("sources"))
  into(layout.buildDirectory.dir("libs"))
  rename { file -> file.replace("-core", "") }
  dependsOn(project(":core").tasks.named("javadocJar"))
}

fun cp(name: String): Provider<FileCollection> {
  return project(":core").tasks.named("${name}Jar").map { it.outputs.files }
}

afterEvaluate {
  tasks.named("generateMetadataFileForMavenPublication").configure {
    dependsOn("copyJavadocAndSources")
  }
}

publishing {
  repositories {
    maven {
      name = "Pl3xRepo"
      url = uri("https://repo.pl3x.net/releases/")
      credentials(PasswordCredentials::class)
      authentication {
        create<BasicAuthentication>("basic")
      }
    }
  }
  publications {
    create<MavenPublication>("maven") {
      groupId = "net.pl3x"
      artifactId = "livemap"
      version = "${rootProject.version}"
      from(components["java"])
    }
  }
}
