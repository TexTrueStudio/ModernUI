# Modern UI
[![CurseForge](http://cf.way2muchnoise.eu/full_352491_downloads.svg)](https://www.curseforge.com/minecraft/mc-mods/modern-ui)
[![CurseForge](http://cf.way2muchnoise.eu/versions/For%20Minecraft_352491_all.svg)](https://www.curseforge.com/minecraft/mc-mods/modern-ui)
[![MavenCore](https://img.shields.io/badge/dynamic/xml?color=orange&label=Core%20Version&query=%2Fmetadata%2Fversioning%2Flatest&url=https%3A%2F%2Fmaven.izzel.io%2Freleases%2Ficyllis%2Fmodernui%2FModernUI-Core%2Fmaven-metadata.xml)]()
[![Discord](https://img.shields.io/discord/696234198767501363?color=green&label=Discord&style=flat)](https://discord.gg/kmyGKt2)
### Description
Modern UI is a desktop application framework designed for standalone 2D and 3D rendering software development.
It makes use of modern 3D graphical APIs and technologies to provide high real-time rendering performance.
For good measure, Modern UI improves and optimizes a set of features used by Android Open Source Project
and its own set of internationalization supporting text layout engine meeting Unicode specification.

There is also an official version that extends to Minecraft and Forge,
which combines Modern UI with Minecraft and provides a large number of additional features and modding APIs.

This project is still at a relatively early stage.  
Releases for Minecraft Mod are available on [CurseForge](https://www.curseforge.com/minecraft/mc-mods/modern-ui).  
If you have any questions or don't know how to get started, feel free to join our [Discord](https://discord.gg/kmyGKt2) server.
### License
* Modern UI
  - Copyright (C) 2019-2022 BloCamLimb. All rights reserved.
  - [![License](https://img.shields.io/badge/License-LGPL--3.0--or--later-blue.svg?style=flat-square)](https://www.gnu.org/licenses/lgpl-3.0.en.html)
  - Copyright (C) 2006 The Android Open Source Project
  - [![License](https://img.shields.io/badge/License-Apache%202.0-orange.svg?style=flat-square)](https://opensource.org/licenses/Apache-2.0)
* Modern UI Assets ─ UI layouts, textures, shaders, models, documents and so on
  - Copyright (C) 2019-2022 BloCamLimb et al.
  - [![License](https://img.shields.io/badge/License-CC%20BY--NC--SA%204.0-yellow.svg?style=flat-square)](https://creativecommons.org/licenses/by-nc-sa/4.0/)
* Libraries
  - [lwjgl](https://github.com/LWJGL/lwjgl3) licensed under the BSD-3-Clause
  - [caffeine](https://github.com/ben-manes/caffeine) by Ben Manes, licensed under the Apache-2.0
  - [flexmark-java](https://github.com/vsch/flexmark-java) by Atlassian Pty Ltd, Vladimir Schneider
  - [fastutil](https://github.com/vigna/fastutil) by Vigna, licensed under the Apache-2.0
  - [RxJava](https://github.com/ReactiveX/RxJava) licensed under the Apache-2.0
  - [log4j](https://github.com/apache/logging-log4j2) licensed under the Apache-2.0
  - [icu4j](https://github.com/unicode-org/icu) by Unicode, Inc.
### Adding Modern UI to your project
#### Environment requirements
- Windows, Linux or macOS (newer macOS may not be supported currently)
- JDK 17.0.1 or above
- OpenGL 4.5 or above (see Mesa Zink for macOS users)
- Vulkan 1.2 or above (not working currently)
- (Optional) Forge 1.18-38.0.15
#### Gradle configuration
```
repositories {
    maven {
        name 'IzzelAliz Maven'
        url 'https://maven.izzel.io/releases/'
    }
}
dependencies {
    implementation "icyllis.modernui:ModernUI-Core:${modernui_version}"
    // apply LWJGL platform here
}
```
##### ForgeGradle 5 (for Minecraft Modding)
```
configurations {
    library
    implementation.extendsFrom library
}
minecraft.runs.all {
    lazyToken('minecraft_classpath') {
        configurations.library.copyRecursive().resolve().collect { it.absolutePath }.join(File.pathSeparator)
    }
}
dependencies {
    library "icyllis.modernui:ModernUI-Core:${modernui_version}"
    implementation fg.deobf("icyllis.modernui:ModernUI-Forge:${minecraft_version}-${modernui_version}")
}
```
Without [MixinGradle](https://github.com/SpongePowered/MixinGradle):
```
minecraft {
    runs {
        client {
            property 'mixin.env.remapRefMap', 'true'
            property 'mixin.env.refMapRemappingFile', "${projectDir}/build/createSrgToMcp/output.srg"
        }
        server {
            property 'mixin.env.remapRefMap', 'true'
            property 'mixin.env.refMapRemappingFile', "${projectDir}/build/createSrgToMcp/output.srg"
        }
        // apply to data if you have datagen
    }
}
```
You need to regenerate run configurations if you make any changes on this.
### Note for Chinese users
- 许可协议即合同，具有法律约束力。无论出于何种目的，只要你使用 Modern UI，即代表你已经阅读并接受了全部条款。
### Screenshots
Navigation  
![new5](https://s2.loli.net/2022/03/06/hwAoHTgZNWBvEdq.png)  
International text  
![new4](https://s2.loli.net/2022/03/06/TM5dVKnpqNvDiJH.png)  
Graphics  
![new3.gif](https://i.loli.net/2021/09/27/yNsL98XtpKP7UVA.gif)  
Audio visualization  
![new2](https://i.loli.net/2021/09/24/TJjyzd6oOf5pPcq.png)  
Out-of-date widgets  
![a](https://i.loli.net/2020/05/15/fYAow29d4JtqaGu.png)
![b](https://i.loli.net/2020/04/10/LDBFc1qo5wtnS8u.png)
