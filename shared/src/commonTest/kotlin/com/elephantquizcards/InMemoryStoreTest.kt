package com.elephantquizcards

import com.elephantquizcards.data.local.InMemoryStore
import com.elephantquizcards.domain.model.CardType
import com.elephantquizcards.domain.model.NodeLevel
import com.elephantquizcards.domain.model.newManualCard
import com.elephantquizcards.domain.repository.subtreeTopicIds
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class InMemoryStoreTest {

    @Test
    fun sample_hasDisciplinesOnlyAtRoot() {
        val store = InMemoryStore.sample()
        val roots = store.childTopics(null)
        assertTrue(roots.isNotEmpty())
        assertTrue(roots.all { it.level == NodeLevel.DISCIPLINE })
    }

    @Test
    fun addCard_appearsInTopic() {
        val store = InMemoryStore.sample()
        val area = store.childTopics("java").first()
        val before = store.cardsByTopic(area.id).size

        val card = newManualCard(area.id, "Q?", "A.", CardType.SELF_CHECK)
        store.upsertCard(card)

        val after = store.cardsByTopic(area.id)
        assertEquals(before + 1, after.size)
        assertNotNull(after.find { it.id == card.id })
    }

    @Test
    fun deleteCard_isSoftDelete() {
        val store = InMemoryStore.sample()
        val card = store.cardsByTopic("java-collections").first()
        store.deleteCard(card.id)

        assertTrue(store.cardsByTopic("java-collections").none { it.id == card.id })
        assertNotNull(store.cardById(card.id)?.deletedAt)
    }

    @Test
    fun subtree_includesDescendants() {
        val ids = InMemoryStore.sample().subtreeTopicIds("java")
        assertTrue("java" in ids)
        assertTrue("java-core" in ids)
        assertTrue("java-collections" in ids)
    }
}
