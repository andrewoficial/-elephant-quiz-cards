package com.elephantquizcards

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.elephantquizcards.data.db.Database
import com.elephantquizcards.data.db.SqlDelightStore
import com.elephantquizcards.domain.model.Card
import com.elephantquizcards.domain.model.CardOption
import com.elephantquizcards.domain.model.CardSource
import com.elephantquizcards.domain.model.CardType
import com.elephantquizcards.domain.model.NodeLevel
import com.elephantquizcards.domain.model.QuestionVariant
import com.elephantquizcards.domain.model.TopicNode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SqlDelightStoreTest {

    private fun newStore(): SqlDelightStore {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        Database.Schema.create(driver)
        return SqlDelightStore.open(driver)
    }

    @Test
    fun topic_roundTrip() {
        val store = newStore()
        val topic = TopicNode(
            id = "t1",
            parentId = null,
            level = NodeLevel.DISCIPLINE,
            name = "Java",
            order = 3,
            color = 0xFFAA0000,
            createdAt = 100,
            updatedAt = 100,
        )
        store.upsertTopic(topic)

        val loaded = store.topicById("t1")
        assertEquals(topic, loaded)
        assertEquals(listOf(topic), store.childTopics(null))
    }

    @Test
    fun card_roundTrip_preservesNestedFields() {
        val store = newStore()
        val card = Card(
            id = "c1",
            createdAt = 10,
            updatedAt = 10,
            type = CardType.MULTIPLE_CHOICE,
            question = "Q?",
            answer = "A",
            questionCode = "code()",
            topicId = "t1",
            options = listOf(
                CardOption("o1", "Первый", true, 0),
                CardOption("o2", "Второй", false, 1),
            ),
            variants = listOf(QuestionVariant("v1", "Q1"), QuestionVariant("v2", "Q2", "c()")),
            labels = listOf("base"),
            tags = listOf("oop", "java"),
            source = CardSource.AI_GENERATED,
            aiUnreviewed = true,
            favorite = true,
            difficulty = 4,
            intervalDays = 7,
            dueDate = 10,
        )
        store.upsertCard(card)

        val loaded = store.cardById("c1")
        assertEquals(card, loaded)
    }

    @Test
    fun deleteCard_isSoftDelete() {
        val store = newStore()
        store.upsertCard(
            Card(
                id = "c1", createdAt = 1, updatedAt = 1,
                type = CardType.SELF_CHECK, question = "Q", answer = "A",
                topicId = "t1", dueDate = 1,
            ),
        )
        store.deleteCard("c1")

        assertTrue(store.cardsByTopic("t1").isEmpty())
        val deleted = store.cardById("c1")
        assertNotNull(deleted)
        assertTrue(deleted.deletedAt != null)
    }

    @Test
    fun cardsByTopics_filtersAndSorts() {
        val store = newStore()
        store.upsertCard(
            Card("a", createdAt = 2, updatedAt = 2, type = CardType.SELF_CHECK,
                question = "Q", answer = "A", topicId = "t1", dueDate = 0),
        )
        store.upsertCard(
            Card("b", createdAt = 1, updatedAt = 1, type = CardType.SELF_CHECK,
                question = "Q", answer = "A", topicId = "t2", dueDate = 0),
        )
        assertEquals(2, store.cardsByTopics(listOf("t1", "t2")).size)
        assertTrue(store.cardsByTopics(emptyList()).isEmpty())
    }
}
