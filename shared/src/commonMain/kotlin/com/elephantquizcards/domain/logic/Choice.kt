package com.elephantquizcards.domain.logic

import com.elephantquizcards.domain.model.Card

/**
 * Проверка ответа методом «варианты ответа»: всё-или-ничего (§3, §9.5 ТЗ).
 *
 * Верно, только если выбраны ровно все верные [Card.options] и ничего лишнего.
 */
fun Card.checkChoice(selectedIds: Set<String>): Boolean {
    val correct = options.filter { it.isCorrect }.map { it.id }.toSet()
    return correct.isNotEmpty() && selectedIds == correct
}