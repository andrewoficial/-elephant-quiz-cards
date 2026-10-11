package com.elephantquizcards

import com.elephantquizcards.data.local.InMemoryStore
import com.elephantquizcards.domain.logic.dueNow
import com.elephantquizcards.domain.logic.todayQueue
import com.elephantquizcards.domain.model.CardOption
import com.elephantquizcards.domain.model.CardType
import com.elephantquizcards.domain.model.NodeLevel
import com.elephantquizcards.domain.model.ReviewMode
import com.elephantquizcards.domain.model.TopicNode
import com.elephantquizcards.domain.model.newManualCard
import kotlin.test.Test
import kotlin.test.assertEquals

class QueueTest {

    private fun topic(
        id: String,
        parentId: String? = null,
        level: NodeLevel = NodeLevel.DISCIPLINE,
    ) = TopicNode(id = id, parentId = parentId, level = level, name = id, createdAt = 0, updatedAt = 0)

    @Test
    fun dueNow_filtersByDateAndSorts() {
        val cards = listOf(
            newManualCard("t1", "Поздняя", "A", now = 300L),
            newManualCard("t1", "Ранняя", "A", now = 100L),
            newManualCard("t1", "Будущая", "A", now = 500L),
        )
        assertEquals(listOf("Ранняя", "Поздняя"), cards.dueNow(300L).map { it.question })
    }

    @Test
    fun dueNow_excludesDeletedAndFuture() {
        val cards = listOf(
            newManualCard("t1", "Срок наступил", "A", now = 100L),
            newManualCard("t1", "Удалённая", "A", now = 100L).copy(deletedAt = 50L),
            newManualCard("t1", "Ещё не срок", "A", now = 200L),
        )
        assertEquals(listOf("Срок наступил"), cards.dueNow(150L).map { it.question })
    }

    @Test
    fun todayQueue_aggregatesSubtree() {
        val topics = listOf(
            topic("java"),
            topic("java-collections", parentId = "java", level = NodeLevel.AREA),
        )
        val cards = listOf(
            newManualCard("java-collections", "Должна", "A", now = 100L),
            newManualCard("java-collections", "Подождёт", "A", now = 400L),
        )
        val store = InMemoryStore.of(topics, cards)

        assertEquals(listOf("Должна"), store.todayQueue("java", 300L).map { it.question })
        assertEquals(2, store.todayQueue("java-collections", 500L).size)
    }

    @Test
    fun todayQueue_filtersByMode() {
        val topics = listOf(topic("java"))
        val shortSelfCheck = newManualCard("java", "Короткий", "char", now = 0)
        val longSelfCheck = newManualCard("java", "Длинный", "A".repeat(120), now = 0)
        val multipleChoice = newManualCard(
            topicId = "java",
            question = "Выбор",
            answer = "A",
            type = CardType.MULTIPLE_CHOICE,
            now = 0,
        ).copy(options = listOf(CardOption("o1", "A", true, 0)))
        val store = InMemoryStore.of(topics, listOf(shortSelfCheck, longSelfCheck, multipleChoice))

        fun queue(mode: ReviewMode) = store.todayQueue("java", mode, 100L).map { it.question }

        assertEquals(listOf("Короткий", "Длинный", "Выбор"), queue(ReviewMode.OPEN))
        assertEquals(listOf("Короткий", "Длинный", "Выбор"), queue(ReviewMode.SELF_CONTROL))
        assertEquals(listOf("Выбор"), queue(ReviewMode.CHOICE))
        assertEquals(listOf("Короткий", "Выбор"), queue(ReviewMode.TYPING))
    }
}