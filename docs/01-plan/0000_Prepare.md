# Stage 00: Prepare — подготовка окружения и каркаса

## Цель
Подготовить рабочее окружение, модульную структуру, Gradle-зависимости,
документацию и минимальный каркас UI, чтобы дальнейшие этапы (Stage 01+)
ложились на готовую почву.

Источник истины: `docs/00-tz/TZ_QuizCards.md` (читать целиком перед любой задачей).

> Примечание по модулям. В этом репозитории модули называются
> `androidApp`, `desktopApp`, `shared`, `iosApp`. В ТЗ фигурирует
> `composeApp` — это тот же слой (точка входа Android/Desktop), просто
> так назван в шаблоне. Держим фактические имена репозитория.

## Статус выполнения (обновлено)

- [x] 0. Окружение
- [x] 1. Модуль `shared`
- [x] 2. Зависимости Gradle (проверены сборкой по одной)
- [~] 3. Документация: `docs/` + `00-tz` + `00-system-requirements` + этот файл +
      `02-architecture/ARCHITECTURE.md` + `DATA-MODEL.md` есть;
      нет `ROADMAP` / `STAGE-*` / `SYNC` / `DECISIONS` / `LIBS` / `GLOSSARY`.
- [~] 4. Каркас UI: сделано **одно** окно (упрощение вместо 4 экранов + навигации)
- [~] 5. Первый билд: `:androidApp:assembleDebug` — OK; установки на телефон ещё не было
- [ ] 6. Git + GitHub + README/LICENSE (сделает автор вручную, локальный git + первый коммит)
- [x] 7. `AGENTS.md` в корне (правило: GUI-тесты — вручную автором)

---

## Легенда

- `[x]` — сделано
- `[~]` — частично
- `[ ]` — не сделано

---

## 0. Окружение (10 мин) — `[x] сделано`

- [x] JDK 21 (Temurin `21.0.7`), `JAVA_HOME` = `C:\Program Files\Eclipse Adoptium\jdk-21.0.7.6-hotspot`.
- [~] Android SDK: `local.properties` → `sdk.dir` **пустой** (сборка держится на `ANDROID_HOME`);
      надо вписать путь явно.
- [x] IntelliJ IDEA (основная IDE) или Android Studio. Gradle — источник истины.
- [x] Проверить версию: `./gradlew --version` → **Gradle 9.5.1**.
      Дистрибутив: `D:\Gradle\gradle-9.5.1`, zip (SHA-256 совпал) подложен в кэш wrapper.

## 1. Модуль `shared` (20 мин) — `[x] сделано`

- [x] В `settings.gradle.kts`: `include(":shared")`.
- [x] `shared/build.gradle.kts` с плагинами:
      `kotlin("multiplatform")`, Android library (`com.android.kotlin.multiplatform.library`),
      `org.jetbrains.compose` (+ `composeCompiler`, `kotlinSerialization`).
- [x] Модуль-заглушка (позже сюда выносим `domain/data/UI`-логику).
- [~] iOS-таргет: в скелете уже объявлены `iosArm64`, `iosSimulatorArm64` —
      не трогаем и на Windows не собираем. Новых не добавляем.

## 2. Зависимости Gradle (20 мин) — `[x] сделано`

Добавляем в `gradle/libs.versions.toml`, затем подключаем в
`shared/build.gradle.kts` и точках входа (`androidApp`, `desktopApp`).

**Важно:** подключаем по одной, с проверкой сборки после каждой.
Иначе `Gradle sync failed` на 200 строк = потерянный день.

- [x] `koin` (core / compose) — **4.2.2** — сборка OK
- [x] `sqldelight` (runtime / android-driver / native-driver) — **2.4.1** — сборка OK
- [x] `ktor` (client-core / client-okhttp) — **3.6.0** — сборка OK
- [x] `kotlinx-serialization-json` (+ Gradle-плагин) — **1.11.0** — сборка OK
- [x] `kotlinx-datetime` — **0.8.0** — сборка OK
- [x] `uuid` (com.benasher44:uuid) — **0.8.4** — сборка OK

> Версии из ТЗ (koin 3.6.0, sqldelight 2.0.2, ktor 2.3.12, serialization 1.7.1,
> datetime 0.6.1, uuid 0.8.2) устарели для Kotlin 2.4.20 — взяты актуальные
> стабильные, проверенные по `maven-metadata.xml`.

Раскладка по source set:

- `commonMain`: koin-core, koin-compose, sqldelight-runtime, ktor-client-core,
  kotlinx-serialization-json, kotlinx-datetime, uuid
- `androidMain`: sqldelight-android-driver, ktor-client-okhttp
- `iosMain`: sqldelight-native-driver (объявлен, на Windows не компилируется)

Целевые записи каталога:

```toml
[versions]
koin = "4.2.2"
sqldelight = "2.4.1"
ktor = "3.6.0"
kotlinx-serialization = "1.11.0"
kotlinx-datetime = "0.8.0"
uuid = "0.8.4"

[libraries]
koin-core = { module = "io.insert-koin:koin-core", version.ref = "koin" }
koin-compose = { module = "io.insert-koin:koin-compose", version.ref = "koin" }
sqldelight-runtime = { module = "app.cash.sqldelight:runtime", version.ref = "sqldelight" }
sqldelight-android = { module = "app.cash.sqldelight:android-driver", version.ref = "sqldelight" }
sqldelight-native = { module = "app.cash.sqldelight:native-driver", version.ref = "sqldelight" }
ktor-client-core = { module = "io.ktor:ktor-client-core", version.ref = "ktor" }
ktor-client-okhttp = { module = "io.ktor:ktor-client-okhttp", version.ref = "ktor" }
kotlinx-serialization-json = { module = "org.jetbrains.kotlinx:kotlinx-serialization-json", version.ref = "kotlinx-serialization" }
kotlinx-datetime = { module = "org.jetbrains.kotlinx:kotlinx-datetime", version.ref = "kotlinx-datetime" }
uuid = { module = "com.benasher44:uuid", version.ref = "uuid" }
```

**Проверка:** `./gradlew :androidApp:assembleDebug` должен собраться без ошибок.

## 3. Документация и структура репо (30 мин) — `[~] частично`

- [x] Создана структура `docs/` и `docs/00-tz/TZ_QuizCards.md` (источник истины).
- [x] `docs/01-plan/0000_Prepare.md` (этот файл).
- [x] `docs/00-system-requirements/SYSTEM-REQUIREMENTS.md` — параметры окружения
      (OS, IDE, JDK, Gradle, SDK, AVD, версии либ).
- [x] `docs/02-architecture/ARCHITECTURE.md` — слои, модули, `expect/actual`, DI, поток данных.
- [x] `docs/02-architecture/DATA-MODEL.md` — сущности + SQL-схема SQLDelight.
- [ ] `ROADMAP.md` и `STAGE-01..07-*.md` — только шаблоны ниже, файлов нет.
- [ ] `02-architecture/SYNC.md`, `DECISIONS.md` — нет.
- [ ] `03-reference/*` — папки созданы, файлов нет.

```
docs/
├── 00-tz/
│   └── TZ_QuizCards.md              # источник истины, as-is
├── 00-system-requirements/
│   └── SYSTEM-REQUIREMENTS.md       # параметры окружения (OS/IDE/JDK/Gradle/SDK/AVD)
├── 01-plan/
│   ├── 0000_Prepare.md              # этот файл
│   ├── ROADMAP.md                   # этапы MVP → post-MVP
│   ├── STAGE-01-foundation.md
│   ├── STAGE-02-domain.md
│   ├── STAGE-03-tree-ui.md
│   ├── STAGE-04-training.md
│   ├── STAGE-05-progress.md
│   ├── STAGE-06-sync-import.md
│   └── STAGE-07-deepseek.md
├── 02-architecture/
│   ├── ARCHITECTURE.md              # слои, модули, потоки данных
│   ├── DATA-MODEL.md                # сущности + SQL-схема
│   ├── SYNC.md                      # Drive + merge rules из §5 ТЗ
│   └── DECISIONS.md                 # ADR-лог (1 пункт = 1 запись)
├── 03-reference/
│   ├── LIBS.md                      # доки KMP, Compose MP, SQLDelight, Koin, Ktor
│   └── GLOSSARY.md                  # SELF_CHECK, TYPING, SM-2, deck и т.д.
└── README.md                        # навигация по docs/
```

### Шаблон ROADMAP.md

```markdown
# Roadmap

## MVP (в порядке реализации)
1. Foundation — модули, DI, БД, навигация.
2. Domain — TopicNode, Card, ReviewLog + репозитории.
3. Tree UI — экран subjects (аккордеон + прогресс-бары).
4. Editor — CRUD карточек, поиск, 3 вкладки.
5. Training — 4 метода + SM-2 + очередь на сегодня.
6. Progress — критерий «выучено», агрегация по дереву.
7. Import/Export — JSON по §5.2/§5.3.
8. Demo deck — Java-колода.
9. Drive sync — OAuth + синк.
10. DeepSeek — 2 навыка из §7.

## Post-MVP
GitHub-публикация, чат с ИИ, режим собеседования, медиа, уведомления,
код-ответы, iOS.
```

### Шаблон STAGE-XX-*.md

```markdown
# Stage NN: <Название>

## Цель
Одно предложение.

## Deliverables
- [ ] ...
- [ ] ...

## Definition of Done
- ...

## Затрагиваемые модули
- shared/domain, shared/data, composeApp/ui/...

## Ссылки на ТЗ
- §...
```

### Шаблон ADR (DECISIONS.md)

```markdown
## ADR-001: SQLDelight вместо Room KMP
Дата: 2025-...
Контекст: ...
Решение: ...
Альтернативы: Room KMP (молодой), ...
Последствия: ...
```

## 4. Каркас UI (40 мин) — `[~] упрощено`

Решение: вместо 4 экранов + навигации делаем **одно окно** — так проще
и быстрее до первого коммита. Навигацию и остальные экраны добавим в Stage-03.

- [x] Одно окно: `shared/src/commonMain/.../App.kt` — `Scaffold` + `TopAppBar`
      с заголовком «Elephant Quiz Cards».
- [x] Экран **Subjects** (`presentation/subjects/SubjectsScreen.kt`): список направлений
      (дисциплин), аккордеон → области/темы с прогресс-барами и процентами.
      Пока на демо-данных (`demoDisciplines()`), домен подключим в Stage-03.
      Проверено на эмуляторе: раскрытие работает (Java → Core/Collections/Concurrency/JVM).
- [ ] Bottom navigation (Subjects / Jungle / Editor / Settings) — отложено.
- [ ] Пустые ViewModel-и (Koin) — отложено.
- [ ] Навигация (`androidx.navigation.compose` / `voyager`) — отложено,
      выбор зафиксировать в ADR.

## 5. Первый билд и установка (15 мин) — `[~]`

- [x] Сборка: `./gradlew :androidApp:assembleDebug` → **BUILD SUCCESSFUL**.
- [ ] Установка на телефон (USB-debugging + `adb install`) — ещё не делалась.

```bash
# сборка
./gradlew :androidApp:assembleDebug

# подключи телефон по USB, включи USB-debugging
adb devices
adb install -r androidApp/build/outputs/apk/debug/androidApp-debug.apk
```

Или из IDE: Run → androidApp (конфигурация создастся автоматически).

### Desktop-итерации (Hot Reload)

```bash
./gradlew :desktopApp:hotRun --auto
```

В IntelliJ IDEA:

- Settings → Compiler → Compose: ✅ Enable Compose Hot Reload.
- Запустить desktop-приложение и править UI вживую.

## 6. Git + GitHub + README/LICENSE (20 мин) — `[ ]` сделает автор

Локальный `git init` + первый коммит автор сделает сам (после этого этапа).
Публикацию на GitHub пока не делаем.

```bash
git init
git add .
git commit -m "chore: initial KMP project skeleton + docs"

gh repo create quizcards --public --source=. --remote=origin --push
```

Обязательно добавить:

- [ ] `LICENSE` — Apache 2.0 (для опенсорса KMP традиционнее) или MIT.
- [ ] `README.md` — описание, скриншот-заглушка, ссылка на `docs/`, «как собрать».
- [ ] `.gitignore` — из шаблона KMP + `local.properties`, `*.keystore`,
      `secrets/`, `google-services.json`.
- [ ] `.gitattributes` — `* text=auto eol=lf` (Windows/Unix без конфликтов).
- [ ] Никогда не коммитить токен DeepSeek. Только через `~/.gradle/gradle.properties`
      или `local.properties` (в `.gitignore`), позже — прокси.

## 7. AGENTS.md (корень репо) — `[x] сделано

Создан `AGENTS.md`. Ключевое правило добавлено в раздел «Правила»:
**GUI-тесты выполняет автор вручную** (эмулятор `Pixel_6` / десктоп из IDEA),
агент UI на эмуляторе автоматически не гоняет — только сборка и юнит-тесты.

Ориентировочное содержимое:

```markdown
# QuizCards — контекст для агента

## Источник истины
- ТЗ: docs/00-tz/TZ_QuizCards.md (читать целиком перед любой задачей)
- Roadmap: docs/01-plan/ROADMAP.md
- Архитектура: docs/02-architecture/ARCHITECTURE.md
- ADR: docs/02-architecture/DECISIONS.md (новые решения — сюда)

## Правила
- Не менять ТЗ. Если противоречие — писать в DECISIONS.md и спрашивать.
- Один stage = одна ветка = один PR.
- Все публичные API — KDoc.
- Тесты: domain-логика (SM-2, merge, критерий «выучено») — обязательно.
- SQL-схема — в shared/sqldelight/*.sq; миграции — не ломать существующие.
- Никаких секретов в коде.

## Текущий stage
docs/01-plan/STAGE-XX-*.md
```

## 8. Запуск и отладка в IDE (IntelliJ IDEA) — `[ ]` делаем постепенно

> Терминология: **uber-jar = fat-jar** — одно и то же (один jar со всеми зависимостями).

### 8.1 Desktop (JVM) — запуск и отладка в IntelliJ IDEA

`mainClass = com.elephantquizcards.MainKt`, файл
`desktopApp/src/main/kotlin/com/elephantquizcards/main.kt`, функция `main()`.

- [x] CLI: `./gradlew :desktopApp:run`
- [x] IDEA, вариант A — «зелёный треугольник»: ▶ у `fun main` в `main.kt` — **работает**,
      окно «Elephant Quiz Cards» открылось. Конфиг был временный (не сохранялся) —
      для переезда добавлен постоянный `Desktop` (Gradle, `:desktopApp:run`) в `.idea/runConfigurations/`.
- [ ] IDEA, вариант B — Gradle-конфигурация: `Run → Edit Configurations → + → Gradle`,
      `Gradle project = ElephantQuizCards`, `Tasks = :desktopApp:run`.
      Плюс: всегда актуален, минус: Debug по Gradle медленнее.
- [ ] IDEA, вариант C (отладка без Gradle-делегата): `+ → Application`,
      Main class = `com.elephantquizcards.MainKt`, `Use classpath of module = desktopApp`
      (иногда модуль зовётся `desktopApp.main`).
- [ ] Проверка: окно «Elephant Quiz Cards» открывается, Debug ловит breakpoint
      в `App.kt` (модуль `shared`).

### 8.2 Compose Hot Reload (живая правка UI)

- [ ] Установить плагин `Compose Hot Reload` (`Settings → Plugins`).
- [ ] Запуск: `./gradlew :desktopApp:hotRun --auto` (или из плагина).
- [ ] Правки `App.kt` в `shared` применяются на лету, без перезапуска.

### 8.3 Android — эмулятор

- [x] AVD уже создан: **Pixel_6** (`android-37.1`, `google_apis_ps16k`, x86_64).
      Список: `emulator -list-avds`.
- [x] Запуск эмулятора: `emulator -avd Pixel_6` → `emulator-5554`, `boot_completed=1`.
- [x] App поставлен и запущен на эмуляторе: `:androidApp:installDebug` +
      `adb shell am start -n com.elephantquizcards/.MainActivity` — экран
      «Elephant Quiz Cards» / «Subjects» отрисовался.
- [x] В IDEA создана конфигурация `androidApp` (тип Android App); экспортирована в
      `.idea/runConfigurations/androidApp.xml` (уезжает через git).
- [ ] Run (▶) / Debug (🐞) прямо из IDEA на выбранном `Pixel_6` — проверить кнопкой.
- [x] CLI-сборка: `./gradlew :androidApp:assembleDebug`
- [x] `adb devices` → эмулятор виден (`emulator-5554`).
- [ ] Прогон тестов на Android (host): `./gradlew :shared:testAndroidHostTest`
      (или `:shared:testDebugUnitTest`).

> Примечание: в IntelliJ IDEA нет полноценного Android-плагина «из коробки», но
> конфигурация типа **Android App** поддерживается. Если кнопка Run у `androidApp`
> не сработает — рабочий путь через Gradle/adb (см. команды выше).

> Примечание: управление AVD (Device Manager) удобнее в Android Studio;
> в IDEA — либо через терминал `emulator -avd Pixel_6`, либо через Android-плагин.

### 8.4 Результаты для проверки на другом компе

- [x] Fat-jar (Desktop):
      `./gradlew :desktopApp:packageUberJarForCurrentOS`
      → `desktopApp/build/compose/jars/com.elephantquizcards-windows-x64-1.0.0.jar`
      (нужна Java 21: `java -jar <файл>`).
- [ ] Портативный дистрибутив со встроенным JRE (Java на целевом ПК не нужна):
      `./gradlew :desktopApp:createDistributable`
      → `desktopApp/build/compose/binaries/main/app/...`
- [ ] Установщик Windows: `./gradlew :desktopApp:packageMsi`
      → `desktopApp/build/compose/binaries/main/msi/com.elephantquizcards-1.0.0.msi`.
- [x] APK: `androidApp/build/outputs/apk/debug/androidApp-debug.apk`.

> Примечание: fat-jar `...-windows-x64-...` собран под текущую ОС. Для Linux/Mac
> пересобрать `packageUberJarForCurrentOS` на соответствующей ОС.

### 8.5 Журнал решений окружения

- **Kotlin/Native (iOS-таргеты на Windows).** Решение: вариант A — дождаться разовой
  загрузки тулчейна. Выполнено: `~\.konan\kotlin-native-prebuilt-windows-x86_64-2.4.20`
  (623 МБ, `provisioned.ok`). В `gradle.properties` добавлен
  `kotlin.native.ignoreDisabledTargets=true`, чтобы не сыпались варнинги про
  отключённые iOS-тесты. iOS-таргеты остаются объявленными (как в ТЗ).
- **Синхронизация run-конфигов.** Решение (временное): разрешить в git
  `.idea/runConfigurations/` (в `.gitignore` заменено `.idea` → `.idea/*` +
  `!.idea/runConfigurations/`), чтобы конфиги ездили домой и обратно. Закрыть через 1–2 недели.
  Экспортированы общие конфиги: `androidApp.xml` (Android App) и `Desktop.xml`
  (Gradle, `:desktopApp:run`).
- **Запуск Android из IDEA.** Полноценного Android-плагина в IDEA нет; рабочие пути —
  конфиг типа Android App (есть) либо Gradle/adb: `emulator -avd Pixel_6` →
  `:androidApp:installDebug` → `adb shell am start -n com.elephantquizcards/.MainActivity`.
- **`sdk.dir`.** В `local.properties` пусто; сборка держится на `ANDROID_HOME=D:\Android\SDK`.
  Прописать явно перед сменой машины.

---

## Тайминг

| Этап                             | Время    |
|----------------------------------|----------|
| Окружение                        | 10 мин   |
| Wizard + модуль shared           | 20 мин   |
| Зависимости в Gradle + сборка    | 20 мин   |
| docs/ (папки + шаблоны + TZ)     | 30 мин   |
| Каркас UI (одно окно)            | 20 мин   |
| Билд + install на телефон        | 15 мин   |
| Git + GitHub + README/LICENSE    | 20 мин   |
| **Итого**                        | ~2.5–3 ч |

## Definition of Done

- [ ] Рабочий APK на телефоне.
- [ ] Репо на GitHub.
- [~] docs-каркас (структура + TZ + 0000; шаблоны-файлы — нет).
- [x] AGENTS.md в корне.
- [x] `./gradlew :androidApp:assembleDebug` без ошибок.

## Грабли

- Не добавлять iOS-таргет сейчас — на Windows не соберётся, сломает синк.
  Добавим на Mac позже.
- Не подключать все либы сразу. По одной, с проверкой сборки после каждой.
- SQLDelight и Room KMP — выбрать одно и зафиксировать в ADR
  (SQLDelight стабильнее для KMP).
- Google Drive OAuth на Android требует `google-services.json` и SHA-1
  отпечатка подписи — не в день MVP.
- DeepSeek-токен — только через `local.properties`/Keystore, никогда в репо.
