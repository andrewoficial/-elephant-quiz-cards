# Модель данных

Источник истины — §2 ТЗ. Здесь: доменные сущности и SQL-схема SQLDelight
(`shared/src/commonMain/sqldelight/com/elephantquizcards/data/db/*.sq`).

## Доменные сущности (`shared/.../domain/model`)

- `TopicNode` + `NodeLevel` (DISCIPLINE→AREA→TOPIC→SECTION→SUBSECTION) — дерево тем.
- `Card` + `CardType` (SELF_CHECK/MULTIPLE_CHOICE), `CardSource` (MANUAL/AI_GENERATED/IMPORTED),
  `CardOption`, `QuestionVariant`. SM-2-поля хранятся на карточке.
- `ReviewLog` + `ReviewMode` (CHOICE/TYPING/OPEN/SELF_CONTROL), `CardState`
  (NEW/LEARNING/REVIEW/ARCHIVED/SUSPENDED).

Логика: `domain/logic/Sm2.kt` (алгоритм SM-2), `domain/logic/Learned.kt`
(применимые формы §3 и критерий «выучено» §4).

## SQL-схема (SQLDelight)

Таблицы: `topic`, `card`, `reviewLog`. Пакет генерации — `com.elephantquizcards.data.db`,
класс `Database`.

### topic
| колонка | тип | заметки |
|---|---|---|
| id | TEXT PK | |
| parentId | TEXT | null только у DISCIPLINE |
| level | TEXT | имя `NodeLevel` |
| name | TEXT | |
| sortOrder | INTEGER | `order` — зарезервированное слово |
| color | INTEGER | nullable |
| createdAt / updatedAt | INTEGER | epoch millis |
| deletedAt | INTEGER | nullable (soft delete) |
| syncVersion | INTEGER | для синка §5.4 |

### card
Скалярные поля `Card` + SM-2. Списки хранятся в JSON-колонках:
`optionsJson`, `variantsJson`, `labelsJson`, `tagsJson`.

> **Упрощение (временное).** Вложенные списки (options/variants/labels/tags) сериализуются
> в JSON, а не выносятся в отдельные таблицы. При необходимости нормализуем схему
> под синк/merge (см. §5 ТЗ) — миграции не ломаем.

### reviewLog
Append-only (§2.3 ТЗ). `selectReviewLogsByCard` для проверки критерия «выучено».

## Реализация

- `data/db/SqlDelightStore.kt` реализует `QuizStore` (`TopicRepository` + `CardRepository`).
- Драйверы: `DatabaseDriverFactory` (expect) → `AndroidSqliteDriver` / `JdbcSqliteDriver` /
  `NativeSqliteDriver`.
  - JVM: файл `quizcards.db`; схема создаётся при первом запуске.
  - Android: `quizcards.db` во внутреннем хранилище приложения.
- Сборка всего этого — через Koin (`di/`): `appModule` регистрирует `single<QuizStore>`,
  `platformModule` — платформенный `DatabaseDriverFactory`. Точки входа вызывают
  `initKoin()` и не знают про БД. См. `ARCHITECTURE.md` §5.
- Первичное наполнение демо-данными — `data/local/SampleData.seedIfEmpty`
  (вызывается в фактори `single<QuizStore>`).

## TODO

- Нормализовать `options`/`variants`/`labels`/`tags` в отдельные таблицы.
- Репозиторий для `ReviewLog` (сейчас схема есть, репозитория нет).
- `syncVersion`/merge по §5.4, форматы deck/progress по §5.2/§5.3.
