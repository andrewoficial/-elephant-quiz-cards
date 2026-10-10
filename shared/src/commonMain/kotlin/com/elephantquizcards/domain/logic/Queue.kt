package com.elephantquizcards.domain.logic

import com.elephantquizcards.domain.model.Card
import com.elephantquizcards.domain.repository.QuizStore
import com.elephantquizcards.domain.repository.cardsInSubtree

/** Карточки с наступившим сроком повторения (не удалённые), ближайшие к сроку — раньше. */
fun List<Card>.dueNow(now: Long): List<Card> =
    filter { it.deletedAt == null && it.dueDate <= now }.sortedBy { it.dueDate }

/**
 * Очередь «сегодня» для узла/области: все карточки поддерева со сроком не позже `now`
 * (§6 ТЗ — «Джунгли: очередь на сегодня»).
 */
fun QuizStore.todayQueue(areaId: String, now: Long): List<Card> =
    cardsInSubtree(areaId).dueNow(now)