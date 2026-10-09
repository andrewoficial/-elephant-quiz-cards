# Архитектура

Источник истины: `docs/00-tz/TZ_QuizCards.md`. Этот документ описывает,
**как** устроен код, а не **что** он делает.

## 1. Главный принцип

Не два проекта, а **один** репозиторий и один Gradle-проект из нескольких модулей.
Код пишется один раз в `shared`, платформы лишь оборачивают его тонкими оболочками.
Различия платформ инкапсулируются через `expect/actual`.

```
ElephantQuizCards/
├── shared/        ← ~90–95% кода: domain + data + UI (Compose Multiplatform)
│   └── src/
│       ├── commonMain/   общий код (все платформы)
│       ├── androidMain/  actual под Android
│       ├── jvmMain/      actual под Desktop/JVM
│       ├── iosMain/      actual под iOS (позже, на Mac)
│       └── commonTest/   общие тесты домена
├── androidApp/    ← MainActivity (Android-вход)
├── desktopApp/    ← main() + Window (Desktop-вход)
└── iosApp/        ← вход для iOS (позже, на Mac)
```

## 2. Модули

| Модуль | Роль | Платформы |
|---|---|---|
| `shared` | Общий код: слои domain/data/presentation, БД, сеть | Android, JVM, iOS |
| `androidApp` | Точка входа Android (`MainActivity`, `App()`) | Android |
| `desktopApp` | Точка входа Desktop (`main()`, `Window`) | JVM |
| `iosApp` | Точка входа iOS (на Windows не собирается) | iOS |

## 3. Слои внутри `shared` (commonMain)

Направление зависимостей — строго внутрь: **presentation → domain ← data**.
Domain не знает ни о Compose, ни о SQLDelight, ни о Ktor.

```
presentation/   UI (Compose) + ViewModel     — зависит от domain
domain/         сущности, use-cases, интерфейсы репозиториев, SM-2, критерий «выучено»
data/           реализации репозиториев: SQLDelight, Ktor, JSON (деки/синк)
di/             модули Koin (связывает интерфейсы с реализациями)
platform/       expect-объявления (драйверы БД, пути, время и т.п.)
```

| Слой | Содержимое | Ссылки на ТЗ |
|---|---|---|
| `domain` | `TopicNode`, `Card`, `ReviewLog`, дерево (5 уровней), SM-2, критерий «выучено», merge-правила | §2, §4, §5.4, §5.5 |
| `data` | SQLDelight-схема и запросы, репозитории, Ktor-клиент (Drive), JSON-форматы deck/progress | §2, §5.1–5.3 |
| `presentation` | Экраны по §6, ViewModel-и, навигация | §6 |

## 4. Source sets и `expect/actual`

- `commonMain` — всё, что не зависит от платформы. Основная масса кода.
- `androidMain` / `jvmMain` / `iosMain` — только реально платформенное.

Правило: если функциональность отличается по платформам — объявляем `expect`
в `commonMain` и `actual` в каждом платформенном source set. Примеры для проекта:

| `expect` (commonMain) | android | jvm | ios |
|---|---|---|---|
| `DatabaseDriverFactory` | `AndroidSqliteDriver` | `JdbcSqliteDriver` | `NativeSqliteDriver` |
| `HttpClientEngine` (Ktor) | `OkHttp` | `OkHttp`/`CIO` | Darwin (позже) |
| `PlatformInfo` | `androidMain` | `jvmMain` | `iosMain` |
| Хранилище токена (Secret Store) | Keystore | файл/Keyring | Keychain (позже) |

Всё остальное (`domain`, большая часть `data`, UI) живёт в `commonMain`.

Подключённые драйверы (Stage 00): `sqldelight-android-driver` (androidMain),
`sqldelight-native-driver` (iosMain). Для JVM-драйвер SQLDelight добавим при
первой интеграции БД.

## 5. Dependency Injection (Koin)

- Модули Koin объявляются в `shared/commonMain` (`di/`).
- Платформенные привязки (`DatabaseDriverFactory`, движок HTTP) добавляются
  через модули, специфичные для платформы, и собираются в общий граф.
- `koin-compose` используется для получения ViewModel во View.
- **Правило:** `expect/actual` для того, что различается; Koin — для сборки графа.

## 6. Поток данных (эскиз)

```
UI (Compose) ──> ViewModel ──> UseCase (domain) ──> Repository (интерфейс, domain)
                                                          │
                                            реализация в data/
                                                          ├── SQLDelight (локально)
                                                          └── Ktor + JSON (Drive/import/export)
```

- Локальная БД — источник истины на устройстве.
- Синхронизация и форматы — по §5 ТЗ (deck-{uuid}.json, progress-{userId}.json).
- ReviewLog — append-only; текущее состояние карточки — снимок.

## 7. Точки входа

| Платформа | Файл | Что делает |
|---|---|---|
| Android | `androidApp/.../MainActivity.kt` | `setContent { App() }` |
| Desktop | `desktopApp/.../main.kt` | `Window { App() }` |
| iOS | `shared/.../MainViewController.kt` | HUD через `ComposeUIViewController` (позже) |

Общий `App()` находится в `shared/commonMain/.../App.kt`. Все UI-изменения —
там, а не в оболочках.

## 8. Правила и конвенции

- Не менять ТЗ; спорные решения — в `docs/02-architecture/DECISIONS.md` (ADR).
- Domain не импортирует Compose/SQLDelight/Ktor.
- SQL-схема — в `shared/sqldelight/*.sq`; миграции не ломают существующие.
- Публичные API — KDoc.
- Тесты домена (SM-2, merge, критерий «выучено») обязательны.
- Никаких секретов в коде (токены — `local.properties`/Keystore).
- Один stage = одна ветка = один PR.

## 9. Текущее состояние (Stage 00)

- [x] Модули `shared`/`androidApp`/`desktopApp`/`iosApp`.
- [x] Зависимости подключены (Koin, SQLDelight, Ktor, serialization, datetime, uuid).
- [x] Одно окно `App()` на Android и Desktop.
- [x] Экран `presentation/subjects/SubjectsScreen` (направления + темы с прогресс-барами),
      данные из `InMemoryStore`.
- [x] Доменные модели: `TopicNode`/`NodeLevel`, `Card`/`CardOption`/`QuestionVariant`,
      `ReviewLog` + enums (§2 ТЗ).
- [x] Доменная логика: SM-2 (`Sm2`) и критерий «выучено» (`isLearned`, `applicableModes`)
      с юнит-тестами.
- [x] Интерфейсы `TopicRepository`/`CardRepository`/`QuizStore`; `InMemoryStore` (тесты/превью).
- [x] Реальное хранилище: SQLDelight (`Database`, схема в `shared/sqldelight/*.sq`) +
      `SqlDelightStore`; драйверы `expect/actual` (Android/JVM/iOS).
- [x] Экран `presentation/editor/EditorScreen` — добавление Q/A; данные сохраняются между запусками.
- [ ] `di/` (Koin) — пока хранилище создаётся в точке входа.
- [ ] Навигация (библиотека) и остальные экраны по §6.
