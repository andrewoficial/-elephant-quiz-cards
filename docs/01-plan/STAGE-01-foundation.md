# Stage 01: Foundation — DI (Koin)

## Цель
Собирать зависимости через Koin вместо ручного создания `SqlDelightStore` и драйвера
в точках входа. Заодно подготовить каркас для ViewModel-ей и навигации.

## Текущее состояние (что уже есть)
- `App(driverFactory)` сам создаёт `SqlDelightStore` через `remember { ... }` и сидит демо-данные.
- Точки входа передают `DatabaseDriverFactory` вручную:
  - Android: `MainActivity` → `DatabaseDriverFactory(applicationContext)`.
  - Desktop: `main.kt` → `DatabaseDriverFactory()`.
  - iOS: `MainViewController` → `DatabaseDriverFactory()`.
- `QuizStore` = `TopicRepository` + `CardRepository`.

## Deliverables
- [ ] Подключить Koin полностью: сейчас в каталоге есть `koin-core`, `koin-compose`.
      Добавить `koin-android` (+ при необходимости `koin-androidx-compose`) для доступа
      к `Context` и Android ViewModel.
- [ ] `shared/commonMain/.../di/AppModule.kt` — `module { single<QuizStore> { SqlDelightStore... } }`
      (или `single { SqlDelightStore.open(get()) }`).
- [ ] `expect val platformModule: Module` (или `expect fun platformModule(): Module`)
      и `actual` для Android/JVM/iOS:
  - Android: `androidContext()` → `AndroidSqliteDriver`.
  - JVM: `JdbcSqliteDriver` (файл `quizcards.db`).
  - iOS: `NativeSqliteDriver`.
- [ ] `fun initKoin()` в common; вызов из точек входа:
  - Android: `Application.onCreate()` (создать `QuizCardsApp : Application`) **или** `MainActivity`.
  - Desktop: `main()` до `application { }`.
  - iOS: в `MainViewController`.
- [ ] `App()` перестаёт принимать `driverFactory` и берёт store из Koin
      (`koinInject()` / `getKoin().get()`).
- [ ] Демо-наполнение `SampleData.seedIfEmpty` перенести в инициализацию.

## Definition of Done
- `./gradlew :androidApp:assembleDebug` и `:desktopApp:run` — собираются и работают.
- `./gradlew :shared:jvmTest` — зелёный (тесты не сломаны).
- В точках входа нет прямого `DatabaseDriverFactory(...)`.
- Автор проверил GUI вручную: данные сохраняются, редактор работает.

## Затрагиваемые модули
- `shared/di`, `shared/data/db`, `androidApp`, `desktopApp`, `shared/iosMain`.

## Ссылки на ТЗ
- §1 (стек: DI — Koin).

## Заметки / решения
- Android требует `Context` для драйвера — решается через `androidContext()` в Koin.
- Выбор: `Application` + `startKoin` (чисто) vs `MainActivity` (минимум правок).
  Зафиксировать в `docs/02-architecture/DECISIONS.md` (ADR).
