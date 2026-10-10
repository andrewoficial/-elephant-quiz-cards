package com.elephantquizcards.presentation.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf

/**
 * Маршруты приложения (§6 ТЗ). Пока реализованы `subjects` и `editor/{areaId}`;
 * остальные (`settings`, `jungle/{areaId}`, `training/{areaId}/{mode}`, ...)
 * добавляются вместе со своими экранами.
 */
sealed interface Screen {
    data object Subjects : Screen
    data class Editor(val areaId: String) : Screen
}

/**
 * Простейший ручной роутер на состоянии Compose.
 *
 * Выбран вместо навигационной библиотеки (см. ADR-003): на 2–5 экранах он проще
 * и не ограничивает сильную кастомизацию UI (планируется стиль «Windows Phone»).
 * При росте числа экранов — пересмотреть и заменить на JetBrains navigation-compose.
 */
class AppNavigator(initial: List<Screen> = listOf(Screen.Subjects)) {

    private val backStack = mutableStateListOf<Screen>().apply { addAll(initial) }

    /** Текущий экран. */
    val current: Screen
        get() = backStack.last()

    /** Кладёт новый экран на стек. */
    fun navigate(screen: Screen) {
        backStack.add(screen)
    }

    /** Возврат к предыдущему экрану; на корневом экране — no-op. */
    fun back() {
        if (backStack.size > 1) {
            backStack.removeAt(backStack.lastIndex)
        }
    }
}