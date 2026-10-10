# QuizCards — контекст для агента

## Источник истины
- ТЗ: `docs/00-tz/TZ_QuizCards.md` (читать целиком перед любой задачей)
- План: `docs/01-plan/0000_Prepare.md`
- Архитектура: `docs/02-architecture/ARCHITECTURE.md`
- ADR: `docs/02-architecture/DECISIONS.md` (новые решения — сюда)
- Окружение: `docs/00-system-requirements/SYSTEM-REQUIREMENTS.md`

## Правила
- Не менять ТЗ. Если противоречие — писать в `DECISIONS.md` и спрашивать.
- Один stage = одна ветка = один PR.
- Все публичные API — KDoc.
- Тесты доменной логики (SM-2, merge, критерий «выучено») — обязательны.
- **GUI-тесты выполняет автор вручную** (эмулятор `Pixel_6` или десктоп из IntelliJ IDEA).
  Агент НЕ гоняет UI автоматически на эмуляторе. Проверка агента ограничивается
  сборкой и юнит-тестами; визуальную проверку интерфейса делает автор и сообщает результат.
- SQL-схема — в `shared/sqldelight/*.sq`; миграции — не ломать существующие.
- Никаких секретов в коде (токены — `local.properties`/Keystore, не в репо).

## Команды проверки
- Сборка Android: `./gradlew :androidApp:assembleDebug`
- Сборка Desktop: `./gradlew :desktopApp:compileKotlin` (или `:desktopApp:run`)
- Юнит-тесты: `./gradlew :shared:jvmTest`
- (GUI — вручную автором.)

## Текущий stage
`docs/01-plan/0000_Prepare.md` — статус. Сделано: Stage-00 (каркас), domain
(Stage-02: модели, SM-2, критерий «выучено», SQLDelight), Stage-01 foundation
(DI/Koin + ручная навигация, ADR-001..003). Следующий шаг: экраны §6
(settings/jungle/training) и полный редактор (CRUD, поиск, вкладки).
