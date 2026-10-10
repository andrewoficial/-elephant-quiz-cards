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

- [x] Подключить Koin полностью: теперь в каталоге есть `koin-core`, `koin-compose`
      (common), `koin-android` (androidMain). `koin-core` — `api` (нужен типам в точках входа).
- [x] `shared/commonMain/.../di/AppModule.kt` — `single<QuizStore> {
      SqlDelightStore.open(get<DatabaseDriverFactory>().createDriver())
      .also(SampleData::seedIfEmpty) }`.
- [x] `expect val platformModule: Module` + `actual` для Android/JVM/iOS:
  - Android: `single<DatabaseDriverFactory> { DatabaseDriverFactory(androidContext()) }`.
  - JVM: `single<DatabaseDriverFactory> { DatabaseDriverFactory() }` (файл `quizcards.db`).
  - iOS: `single<DatabaseDriverFactory> { DatabaseDriverFactory() }` (`NativeSqliteDriver`).
- [x] `fun initKoin()` в common + вызов из точек входа:
  - Android: `QuizCardsApp : Application` → `initKoin(context)` в `onCreate()`.
  - Desktop: `main()` до `Window`.
  - iOS: в `MainViewController`.
- [x] `App()` больше не принимает `driverFactory`; `QuizStore` из Koin через `koinInject()`.
- [x] Демо-наполнение `SampleData.seedIfEmpty` перенесено в инициализацию (фактори `single`).
- [x] Навигация (ручной роутер): `presentation/navigation/AppNavigator.kt`, маршруты
      `subjects` / `editor/{areaId}`. Выбор — в ADR-003.

## Definition of Done

- [x] `./gradlew :androidApp:assembleDebug` и `:desktopApp:compileKotlin` — BUILD SUCCESSFUL.
- [x] `./gradlew :shared:jvmTest` — зелёный (вкл. новый `KoinModuleTest`).
- [x] В точках входа нет прямого `DatabaseDriverFactory(...)`.
- [ ] Автор проверил GUI вручную: данные сохраняются, редактор работает, навигация кликается.

## Затрагиваемые модули
- `shared/di`, `shared/data/db`, `androidApp`, `desktopApp`, `shared/iosMain`.

## Ссылки на ТЗ
- §1 (стек: DI — Koin).

## Заметки / решения
- Android требует `Context` для драйвера — решается через `androidContext()` в Koin.
- Выбор: `Application` + `startKoin` (чисто) vs `MainActivity` (минимум правок).
  Зафиксировано в `docs/02-architecture/DECISIONS.md` (ADR-002): взят `Application`.
- Роутер: ручной `AppNavigator` на состоянии Compose вместо библиотеки — ADR-003.
