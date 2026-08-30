<!--suppress ALL -->
<div align="center">

<picture>
  <source media="(prefers-color-scheme: dark)" srcset="https://raw.githubusercontent.com/BillyGalbreath/LiveMap/v4/.github/images/og.png">
  <source media="(prefers-color-scheme: light)" srcset="https://raw.githubusercontent.com/BillyGalbreath/LiveMap/v4/.github/images/og.png">
  <img src="https://raw.githubusercontent.com/BillyGalbreath/LiveMap/v4/.github/images/og.png" alt="LiveMap" width="700">
</picture>

[![][Version Badge]][Modrinth Url]
[![][Modrinth Badge]][Modrinth Url]
[![][Jenkins Badge]][Jenkins Url]  
[![][Discord Badge]][Discord Url]
[![][MIT Badge]][License Url]
[![][CodeFactor Badge]][CodeFactor Url]  
[![][bStats Badge]][bStats Url]
[![][Stars Badge]][Stars Url]
[![][Forks Badge]][Forks Url]
[![][Watchers Badge]][Watchers Url]

##
<br/>

<big><b>LiveMap</b> is a high-performance, minimalistic world map viewer for Minecraft  
servers. Built as the official evolution of Pl3xMap, it delivers crisp, vanilla-style 2D  
rendering with a fraction of the resource overhead found in traditional map plugins.</big>

</div>

<!-- @formatter:off -->

## 🚀 Features

* **⚡ Near-instant generation:** Renders complete in minutes rather than days, minimizing initial setup, update times, and server CPU overhead.
* **🗺️ Vanilla-inspired 2D UI:** Features a lightweight, top-down perspective optimized for clear world navigation without the performance tax of 3D rendering.
* **👥 Real-time player tracking:** Displays player markers with precise yaw rotation, health, and armor statuses.
* **🎨 Extensible render types:** Includes multiple built-in map styles out of the box, including Fancy, Basic, Biomes, Inhabited, and Flowermap.
* **🌐 Modern Leaflet frontend:** Built on a highly responsive, up-to-date Leaflet.js framework for a fast, modern browser experience.
* **🔌 Robust developer API:** Provides a powerful API interface to build custom addons or seamless third-party plugin integrations with ease.

## ⬇️ Downloads

Downloads are available on Modrinth.

[![Download on Modrinth](https://i.imgur.com/5C4fVJC.png)](https://modrinth.com/plugin/livemap)

## 🌐 Live Demo

See LiveMap in action and compare its performance side-by-side with alternative map viewers:

🔗 [Live Demo Framework](https://roanv.nl)

## 📈 bStats

[![bStats Graph Data][bStats Graph]][bStats Url]

## 👨‍💻 Developers

<details>
<summary>🪶 Maven</summary>

```xml
<repository>
  <id>pl3x-repo</id>
  <url>https://repo.pl3x.net/releases/</url>
</repository>
<dependency>
  <groupId>net.pl3x</groupId>
  <artifactId>livemap</artifactId>
  <version>4.0.0</version>
  <scope>provided</scope>
</dependency>
```
</details>

<details>
<summary>🐘 Gradle (Groovy DSL)</summary>

```groovy
repositories {
    maven { url = "https://repo.pl3x.net/releases/" }
}
dependencies {
    compileOnly 'net.pl3x:livemap:4.0.0'
}
```
</details>

<details open>
<summary>⚙️ Gradle (Kotlin DSL)</summary>

```groovy
repositories {
    maven("https://repo.pl3x.net/releases/")
}
dependencies {
    compileOnly("net.pl3x:livemap:4.0.0")
}
```
</details>

## 🛠️ Building from source

Just run the following command:

```
./gradlew build
```

The compiled jars will be in `/build/libs/`

<!-- Links -->
[Jenkins Url]: https://ci.pl3x.net/job/LiveMap/
[Modrinth Url]: https://modrinth.com/project/LiveMap/
[Discord Url]: https://discord.gg/JXra7N4

[License Url]: https://github.com/BillyGalbreath/LiveMap/blob/v4/LICENSE
[CodeFactor Url]: https://www.codefactor.io/repository/github/BillyGalbreath/LiveMap

[bStats Url]: https://bstats.org/plugin/bukkit/LiveMap/26542
[Stars Url]: https://github.com/BillyGalbreath/LiveMap/stars
[Forks Url]: https://github.com/BillyGalbreath/LiveMap/forks
[Watchers Url]: https://github.com/BillyGalbreath/LiveMap/watchers

<!-- Images -->
[Jenkins Badge]: https://img.shields.io/jenkins/build?jobUrl=https%3A%2F%2Fci.pl3x.net%2Fjob%2FLiveMap%2F&logo=Jenkins
[Version Badge]: https://img.shields.io/badge/dynamic/json?url=https%3A%2F%2Fci.pl3x.net%2Fjob%2FLiveMap%2FlastSuccessfulBuild%2FinjectedEnvVars%2Fapi%2Fjson&query=$.envMap.livemap&label=v&color=green
[Modrinth Badge]: https://pl3x.net/badge/modrinth/livemap
[Discord Badge]: https://img.shields.io/discord/171810209197457408.svg?label=&logo=discord&logoColor=ffffff&color=7389D8&labelColor=6A7EC2

[MIT Badge]: https://img.shields.io/github/license/BillyGalbreath/LiveMap?&logo=github
[CodeFactor Badge]: https://pl3x.net/badge/codefactor/github/billygalbreath/livemap

[bStats Badge]: https://img.shields.io/bstats/servers/26542
[Stars Badge]: https://img.shields.io/github/stars/BillyGalbreath/LiveMap?label=stars&logo=github
[Forks Badge]: https://img.shields.io/github/forks/BillyGalbreath/LiveMap?label=forks&logo=github
[Watchers Badge]: https://img.shields.io/github/watchers/BillyGalbreath/LiveMap?label=watchers&logo=github

[bStats Graph]: https://bstats.org/signatures/bukkit/LiveMapMC.svg
