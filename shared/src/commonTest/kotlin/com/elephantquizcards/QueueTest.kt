package com.elephantquizcards

import com.elephantquizcards.data.local.InMemoryStore
import com.elephantquizcards.domain.logic.dueNow
import com.elephantquizcards.domain.logic.todayQueue
import com.elephantquizcards.domain.model.NodeLevel
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
}