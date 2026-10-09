# System Requirements — рабочее окружение

Актуальные параметры окружения разработки. Обновлять при изменении
версий IDE / SDK / зависимостей. Проверено: 2026-10-09.

## Операционная система

| Параметр | Значение |
|---|---|
| OS | Windows 10 Pro, Build **19045** (22H2), x64 |
| Оболочка | PowerShell 7+ |

## IDE

| Параметр | Значение |
|---|---|
| Основная IDE | IntelliJ IDEA |
| Установленные версии | `2024.1.6`, `2025.1.3`, **`2026.1.1`** (рабочая) |
| Директория | `C:\Program Files\JetBrains\IntelliJ IDEA 2026.1.1` |

> Android Studio используется только при необходимости (Profiler / Layout Inspector /
> Device Manager). Проект IDE-независим: Gradle — источник истины.

## JDK / JVM

| Параметр | Значение |
|---|---|
| JDK | Temurin **21.0.7** LTS (`21.0.7+6`) |
| `JAVA_HOME` | `C:\Program Files\Eclipse Adoptium\jdk-21.0.7.6-hotspot\` |
| Gradle daemon toolchain | **Azul Zulu 21** (`gradle/gradle-daemon-jvm.properties`) |

## Gradle

| Параметр | Значение |
|---|---|
| Wrapper | **9.5.1** (`gradle/wrapper/gradle-wrapper.properties`) |
| Локальная копия | `D:\Gradle\gradle-9.5.1` (zip `gradle-9.5.1-bin.zip`, SHA-256 совпал) |
| Кэш wrapper | `D:\Android\.gradle\wrapper\dists\gradle-9.5.1-bin\...` |
| Configuration cache | включён (`org.gradle.configuration-cache=true`) |

## Kotlin / Gradle-плагины

| Плагин | Версия |
|---|---|
| Kotlin | **2.4.20** |
| Android Gradle Plugin (AGP) | **9.1.1** |
| Compose Multiplatform | **1.12.1** |
| Kotlin Compose Compiler | 2.4.20 (ref `kotlin`) |
| Kotlin Serialization | 2.4.20 (ref `kotlin`) |

## Android SDK

| Параметр | Значение |
|---|---|
| `ANDROID_HOME` | `D:\Android\SDK` |
| `local.properties` `sdk.dir` | **пусто** (сборка держится на `ANDROID_HOME`) ⚠️ |
| Platforms | `android-36`, `android-37.0` |
| Build-tools | `35.0.0`, `36.0.0` |
| platform-tools / adb | 37.0.1 / ADB 1.0.41 (`D:\Android\SDK\platform-tools\adb.exe`) |
| compileSdk / targetSdk | **37** |
| minSdk | **24** |

## Эмулятор / AVD

| Параметр | Значение |
|---|---|
| AVD home | `D:\Android\avd` (`ANDROID_AVD_HOME`) |
| AVD | **Pixel_6** |
| Target / образ | `android-37.1`, tag `google_apis_ps16k`, abi `x86_64` |
| Разрешение | 1080 x 2400, 420 dpi |
| System image | `D:\Android\SDK\system-images\android-37.1\google_apis_ps16k\x86_64` |

## Ключевые библиотеки

| Библиотека | Версия |
|---|---|
| Koin (`koin-core`, `koin-compose`) | **4.2.2** |
| SQLDelight (`runtime`, `android-driver`, `native-driver`) | **2.4.1** |
| Ktor (`client-core`, `client-okhttp`) | **3.6.0** |
| kotlinx-serialization-json | **1.11.0** |
| kotlinx-datetime | **0.8.0** |
| uuid (com.benasher44) | **0.8.4** |
| kotlinx-coroutines | **1.11.0** |
| androidx-lifecycle (KMP) | **2.11.0** |
| Compose Material3 (KMP) | **1.12.0-alpha03** |

## Модули проекта

| Модуль | Назначение |
|---|---|
| `shared` | KMP: domain/data/UI (Android + JVM + iOS-таргеты объявлены) |
| `androidApp` | Android-точка входа (`com.elephantquizcards.MainActivity`) |
| `desktopApp` | Desktop/JVM-точка входа (`com.elephantquizcards.MainKt`) |
| `iosApp` | iOS-точка входа (на Windows не собирается) |

## Проверка окружения

```powershell
java -version                                  # ожидаем 21.x
.\gradlew.bat --version                         # ожидаем Gradle 9.5.1
adb version                                     # ADB 1.0.41 (37.0.1)
emulator -list-avds                             # ожидаем Pixel_6
.\gradlew.bat :androidApp:assembleDebug         # BUILD SUCCESSFUL
```

## Известные замечания

- `sdk.dir` в `local.properties` пустой — сборка работает только потому, что задан
  `ANDROID_HOME`. Рекомендуется прописать `sdk.dir=D\:\\Android\\SDK`.
- iOS-таргеты объявлены в `shared`, но на Windows не компилируются — это ожидаемо.
- Fat-jar (`...-windows-x64-...`) собирается под текущую ОС; для Linux/Mac —
  пересобрать `:desktopApp:packageUberJarForCurrentOS` на соответствующей ОС.
- **Skiko (Desktop):** в логе возможен `Failed to create DirectX12 device` +
  `[SKIKO] warn: Fallback to next API`. Это НЕ ошибка — рендер откатывается на другой
  backend и приложение работает (типично для RDP/ВМ/старых драйверов GPU).
  Форсировать backend при необходимости: JVM-флаг `-Dskiko.renderApi=OPENGL` (или `SOFTWARE`).
- **SLF4J (Desktop):** раньше печаталось `No SLF4J providers were found` — устранено
  добавлением `org.slf4j:slf4j-nop` (runtimeOnly) в `desktopApp`.
