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

#

<br/>

<b>LiveMap</b> is a high-performance, minimalistic world map viewer for Minecraft  
servers. Built as the official evolution of Pl3xMap, it delivers crisp, vanilla-style 2D  
rendering with a fraction of the resource overhead found in traditional map plugins.

</div>

<br/>

<!-- @formatter:off -->

# 🚀 Features

* **⚡ Near-instant generation:**  
  &emsp;&nbsp; Renders complete in minutes rather than days, minimizing initial setup, update times, and server CPU overhead.

* **🗺️ Vanilla-inspired 2D UI:**  
  &emsp;&nbsp; Features a lightweight, top-down perspective optimized for clear world navigation without the performance tax of 3D rendering.

* **👥 Real-time player tracking:**  
  &emsp;&nbsp; Displays player markers with precise yaw rotation, health, and armor statuses.

* **🎨 Extensible render types:**  
  &emsp;&nbsp; Includes multiple built-in map styles out of the box, including Fancy, Basic, Biomes, Inhabited, and Flowermap.

* **🌐 Modern Leaflet frontend:**  
  &emsp;&nbsp; Built on a highly responsive, up-to-date Leaflet.js framework for a fast, modern browser experience.

* **🔌 Robust developer API:**  
  &emsp;&nbsp; Provides a powerful API interface to build custom addons or seamless third-party plugin integrations with ease.

<br/>

# Contact

[![][Discord Badge]][Discord Url]

Join me on [Discord][Discord Url]!

<br/>

# ⬇️ Downloads

[![Download on Modrinth](https://i.imgur.com/5C4fVJC.png)][Modrinth Url]

Downloads are available on [Modrinth][Modrinth Url].

<br/>

# 🌐 Live Demo

See LiveMap in action and compare its performance side-by-side with alternative map viewers:

🔗 [Live Demo](https://roanv.nl) hosted by [@Roan-V](https://github.com/Roan-V)

<br/>

# 📊 Data Collection & Telemetry

This plugin uses [**bStats**][bStats Url] to anonymously collect usage data. This data helps me understand how the plugin is being used and what features to focus on in future updates.

* **📈 What is collected:** General server information such as server software version, system architecture, core count, Java version, online mode, player count, and plugin-specific metrics (e.g., config settings).

* **❌ What is NOT collected:** Strictly no personally identifiable information (PII) is ever tracked. bStats explicitly prohibits and strips IP addresses, player names, chat logs, or UUIDs.

* **🚫 How to opt out:** You can easily disable telemetry globally by navigating to your server's `/plugins/bStats/config.yml` file and setting `enabled: false`.

<br/>

[![bStats Graph Data][bStats Graph]][bStats Url]

<br/>

# License

[![][MIT Badge]][License Url]

All code is licensed under MIT license, unless otherwise noted.

<br/>

# 👨‍💻 Developers

To use LiveMap in your own project, 

<details>
<summary>🪶 Maven</summary>

```xml
<repository>
  <id>pl3x-releases</id>
  <name>Pl3x Repository</name>
  <url>https://repo.pl3x.net/releases</url>
</repository>
<dependency>
  <groupId>net.pl3x</groupId>
  <artifactId>livemap</artifactId>
  <version>[4.0.0,)</version>
  <scope>provided</scope>
</dependency>
```
</details>

<details>
<summary>🐘 Gradle (Groovy DSL)</summary>

```groovy
repositories {
  maven {
    name "pl3x-releases"
    url "https://repo.pl3x.net/releases"
  }
}
dependencies {
  compileOnly "net.pl3x:livemap:[4.0.0,)"
}
```
</details>

<details open>
<summary>⚙️ Gradle (Kotlin DSL)</summary>

```kotlin
repositories {
  maven {
    name = "pl3x-releases"
    url = uri("https://repo.pl3x.net/releases")
  }
}
dependencies {
  compileOnly("net.pl3x:livemap:[4.0.0,)")
}
```
</details>

<br/>

# 🛠️ Building from source

Requires JDK 25.

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
[Modrinth Badge]: https://pl3x.net/badge/modrinth/dt/livemap?logo=modrinth&
[Discord Badge]: https://img.shields.io/discord/171810209197457408.svg?label=&logo=discord&logoColor=ffffff&color=7389D8&labelColor=6A7EC2

[MIT Badge]: https://img.shields.io/github/license/BillyGalbreath/LiveMap?&logo=github
[CodeFactor Badge]: https://pl3x.net/badge/codefactor/grade/github/billygalbreath/livemap?label=codefactor&logo=codefactor&

[bStats Badge]: https://pl3x.net/badge/bstats/servers/26542?logo=bstats&
[Stars Badge]: https://pl3x.net/badge/github/stars/BillyGalbreath/LiveMap?label=stars&logo=stars&
[Forks Badge]: https://pl3x.net/badge/github/forks/BillyGalbreath/LiveMap?label=forks&logo=forks&
[Watchers Badge]: https://pl3x.net/badge/github/watchers/BillyGalbreath/LiveMap?label=watchers&logo=watchers&

[bStats Graph]: https://bstats.org/signatures/bukkit/LiveMapMC.svg
