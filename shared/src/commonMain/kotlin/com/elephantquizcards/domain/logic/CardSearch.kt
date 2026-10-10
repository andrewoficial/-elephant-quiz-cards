package com.elephantquizcards.domain.logic

import com.elephantquizcards.domain.model.Card

/**
 * Поиск карточек по тексту (вопрос/ответ, без учёта регистра).
 *
 * Пустой запрос матчит всё. Используется в редакторе для фильтрации длинных списков.
 */
fun Card.matches(query: String): Boolean {
    if (query.isBlank()) return true
    val needle = query.trim()
    return question.contains(needle, ignoreCase = true) ||
        answer.contains(needle, ignoreCase = true)
}

/** Фильтрует карточки по поисковому запросу, сохраняя порядок. */
fun List<Card>.search(query: String): List<Card> = filter { it.matches(query) }