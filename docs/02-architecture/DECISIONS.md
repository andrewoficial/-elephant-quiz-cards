# Журнал решений (ADR)

1 запись = 1 решение. Новые решения дописывать сюда (см. `AGENTS.md`).

## ADR-001: SQLDelight вместо Room KMP
Дата: 2026-10-10
Статус: принято
Контекст: локальная БД должна работать из общего кода на Android/JVM/iOS.
Решение: SQLDelight 2.4.1 — схема в `.sq`-файлах, код генерируется в сборку,
  один SQL для всех платформ.
Альтернативы: Room KMP (молодой, меньше возможностей в KMP), ручной SQLite
  (нет типизации и генерации).
Последствия: единый источник истины — `shared/sqldelight/*.sq`; миграции — через
  SQLDelight и не ломают существующую схему.

## ADR-002: DI — Koin, старт из Application
Дата: 2026-10-10
Статус: принято
Контекст: точки входа сами создавали `DatabaseDriverFactory` и `SqlDelightStore`.
Решение: Koin-граф в `shared/di`: `appModule` (common) + `platformModule`
  (expect/actual). Android — `QuizCardsApp : Application` вызывает `initKoin(context)`
  в `onCreate()` (контекст живёт дольше Activity и доступен раньше). Desktop —
  `initKoin()` в `main()` до `Window`. iOS — в `MainViewController`. `App()` получает
  `QuizStore` через `koinInject`.
Альтернативы: старт в `MainActivity.onCreate` (меньше файлов, но контекст
  пересоздаётся вместе с Activity).
Последствия: в точках входа нет `DatabaseDriverFactory(...)`; драйвер БД
  регистрируется в `platformModule` каждой платформы; `koin-core` объявлен `api`
  в `commonMain`, чтобы типы Koin были видны точкам входа.

## ADR-003: Роутер — ручной, на состоянии Compose (без библиотеки)
Дата: 2026-10-10
Статус: принято
Контекст: на этапе 2–5 экранов внешняя библиотека навигации даёт больше
  ограничений, чем пользы; планируется сильная кастомизация оформления
  (стиль «Windows Phone»).
Решение: `presentation/navigation/AppNavigator.kt` — `sealed interface Screen`
  + простой back-stack на `mutableStateListOf`; переходы — `navigate()/back()`.
Альтернативы: JetBrains navigation-compose 2.9.2 (type-safe маршруты, но свои
  сценарии анимаций/жестов), Voyager.
Последствия: при росте числа экранов (jungle/training/settings/free_training/
  interview) переоценить решение и заменить на библиотеку — правка здесь, в этом файле.