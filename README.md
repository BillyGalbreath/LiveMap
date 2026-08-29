<!--suppress ALL -->
<div align="center">

<picture>
  <source media="(prefers-color-scheme: dark)" srcset="https://raw.githubusercontent.com/BillyGalbreath/LiveMap/v4/.github/images/og.png">
  <source media="(prefers-color-scheme: light)" srcset="https://raw.githubusercontent.com/BillyGalbreath/LiveMap/v4/.github/images/og.png">
  <img src="https://raw.githubusercontent.com/BillyGalbreath/LiveMap/v4/.github/images/og.png" alt="LiveMap" width="700">
</picture>

[![][Version Badge]][Modrinth Url]
[![][Downloads Badge]][Modrinth Url]
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
[Downloads Badge]: https://img.shields.io/modrinth/dt/LiveMap?logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAABhWlDQ1BJQ0MgcHJvZmlsZQAAKJF9kT1Iw0AcxV9bxSIVh1YQcYhQnSyIiuimVShChVArtOpgcukXNGlIUlwcBdeCgx+LVQcXZ10dXAVB8APE0clJ0UVK/F9SaBHjwXE/3t173L0D/PUyU82OMUDVLCOViAuZ7KrQ9YogwujDEGYkZupzopiE5/i6h4+vdzGe5X3uz9Gj5EwG+ATiWaYbFvEG8dSmpXPeJ46woqQQnxOPGnRB4keuyy6/cS447OeZESOdmieOEAuFNpbbmBUNlXiSOKqoGuX7My4rnLc4q+Uqa96TvzCU01aWuU5zEAksYgkiBMioooQyLMRo1UgxkaL9uId/wPGL5JLJVQIjxwIqUCE5fvA/+N2tmZ8Yd5NCcaDzxbY/hoGuXaBRs+3vY9tunACBZ+BKa/krdWD6k/RaS4seAb3bwMV1S5P3gMsdoP9JlwzJkQI0/fk88H5G35QFwrdA95rbW3Mfpw9AmrpK3gAHh8BIgbLXPd4dbO/t3zPN/n4Ax9dyyerighsAAAAGYktHRAAAAAAAAPlDu38AAAAJcEhZcwAADdcAAA3XAUIom3gAAAAHdElNRQfmCBMVIw496dpyAAACm0lEQVQ4y2XTTYjVdRTG8c/vd//33sQJZyEVrYZeNJ1BSgKTsVbekRRbZVSrIqHwNUxtFVkboXyBQnORK6kICXQxSaOG0oSQiJAz4xuaUWvvCFdr7p37Py0Uszyrs3j4HnjO8yT/n4mhATlWS5YKfSC5JhxTpi/NHxm7V57ubuOrairN3aR30BRpWI4rIDyB5eiVfGG69z39B9v/AsZX1VQmj+AF4UM9rd2m63XtYsBUfdyC4aY/Fs9wc+Ym0jac1O1drv9gu4A7l5+nXInTWg/uk+I1VNWnHnexscUtj5rOm1TLs8JhxY0d2JBNDA2Q3ha2mS5+kfKoFK8I26W8yJxjv+Gq8KqiHDVVO0X6SMQa48v6sxyrMamntVu13IU+kZaoxS5RLnRp6T5FHBRpEI+ptXeK+Ik0pVJ+loUGvjddrwuvC58oKmM66RD2CrM0m7fMGzkj4lvhTckJoi48k1xovKRMV+WYhVEpL6KcLQyTVntqZP/tT6yqOD+5TDZMfpfubGzM9+UgpwqpDzeleNr5xpALjbMuTu4krt+GxWkpdSAL2+V4XzXGiI6y+52IPZJzwjrJD5IeRdopW4G2ojKmdBOXs+QoVujkvaSqiEfktNHco4tFvKxMQ2LGQtPxMGkLDmj3/K3s/Vyr+Vwy1hhUGJXiL/LHyvINKfURnyoNKyuXFOU8HBeueKAzqF3dIKzTrs3NBo7+jBGRqsSvOvXF+Jq0VU6nFOVWZfdP4iud2hKdYlD4AN9YMNy8bWK3dyVOCofVOmvNbK1VjYewRDcfMP/H37VmrFdvr1emQyKd0Lq++f4yFTd2iFiDSRyRXL7j+hzSi5iFPVrXN3v2TOe/gLugZf1y9y0pNbhTZ64JI4pyvyePT9wr/wdL6hgY+Xe2UgAAAABJRU5ErkJggg==
[Discord Badge]: https://img.shields.io/discord/171810209197457408.svg?label=&logo=discord&logoColor=ffffff&color=7389D8&labelColor=6A7EC2

[MIT Badge]: https://img.shields.io/github/license/BillyGalbreath/LiveMap?&logo=github
[CodeFactor Badge]: https://img.shields.io/codefactor/grade/github/BillyGalbreath/LiveMap?logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8%2F9hAAABhWlDQ1BJQ0MgcHJvZmlsZQAAKJF9kT1Iw0AcxV9bxSIVh1YQcYhQnSyIiuimVShChVArtOpgcukXNGlIUlwcBdeCgx%2BLVQcXZ10dXAVB8APE0clJ0UVK%2FF9SaBHjwXE%2F3t173L0D%2FPUyU82OMUDVLCOViAuZ7KrQ9YogwujDEGYkZupzopiE5%2Fi6h4%2BvdzGe5X3uz9Gj5EwG%2BATiWaYbFvEG8dSmpXPeJ46woqQQnxOPGnRB4keuyy6%2FcS447OeZESOdmieOEAuFNpbbmBUNlXiSOKqoGuX7My4rnLc4q%2BUqa96TvzCU01aWuU5zEAksYgkiBMioooQyLMRo1UgxkaL9uId%2FwPGL5JLJVQIjxwIqUCE5fvA%2F%2BN2tmZ8Yd5NCcaDzxbY%2FhoGuXaBRs%2B3vY9tunACBZ%2BBKa%2FkrdWD6k%2FRaS4seAb3bwMV1S5P3gMsdoP9JlwzJkQI0%2Ffk88H5G35QFwrdA95rbW3Mfpw9AmrpK3gAHh8BIgbLXPd4dbO%2Ft3zPN%2Fn4Ax9dyyerighsAAAAGYktHRAAAAAAAAPlDu38AAAAJcEhZcwAADdcAAA3XAUIom3gAAAAHdElNRQfmCBMVKAA5pS6%2BAAABlElEQVQ4y82PP2gVQRDGf7N3t%2Bvdixpi0N5OELFKJ1iohBciKlgYJLX6YkBbC0sVooVFBAvBPw%2BFZzrJs7DR2iYHRhBsxNI8VLwUx92MRXJGxKCp9AfL7DfDfPutFO3z5wy5DuRlWU2OvLj7hduLYXh0ZSEkOh4SjUKiBK%2BEZP34Gu%2FtbebLE86Qa8BO4FDwyWmAbPjzMWACiNgEMdun6macwfJ6z2qxZYBI6ndAxR%2BRN%2FL1ZGeXlDqFkm%2Fv33nZjHZ0u2OZrw%2F7pBYf16Re8UEJ8VpNE33fP3BxgX%2BOFOOdtjmuGpoPtT51pNcrMZORx4%2FmslQnslAlWahItymZrz%2Bmqc4%2B2z%2B71BjE5uwesEeQsaLY%2FQp42LrfPUqwy2DNO03ZK9hN4Ehj4IDBjzjKCoC5aMDG9q%2BhBz%2BrWCN3KqptBtG89Xx%2BEWB1%2Bszr8OTBFMgkSLKWQAA%2BVCU3%2BK%2BQb%2B0LB4FLGHmrP39LNv3773Ei9IBphLnVduf4VhM4M9JGqGzc%2F5bYnDsrqlcQloaK0adbNfgOUn6NRlZZ46YAAAAASUVORK5CYII%3D

[bStats Badge]: https://img.shields.io/bstats/servers/26542
[Stars Badge]: https://img.shields.io/github/stars/BillyGalbreath/LiveMap?label=stars&logo=github
[Forks Badge]: https://img.shields.io/github/forks/BillyGalbreath/LiveMap?label=forks&logo=github
[Watchers Badge]: https://img.shields.io/github/watchers/BillyGalbreath/LiveMap?label=watchers&logo=github

[bStats Graph]: https://bstats.org/signatures/bukkit/LiveMapMC.svg
