package com.elephantquizcards.data.local

import com.elephantquizcards.domain.model.Card
import com.elephantquizcards.domain.model.ReviewLog
import com.elephantquizcards.domain.model.TopicNode
import com.elephantquizcards.domain.repository.QuizStore
import com.elephantquizcards.platform.currentTimeMillis

/**
 * Простое хранилище в памяти (для тестов и превью). В приложении используется
 * [com.elephantquizcards.data.db.SqlDelightStore].
 */
class InMemoryStore private constructor(
    initialTopics: List<TopicNode>,
    initialCards: List<Card>,
) : QuizStore {

    private val topics: MutableMap<String, TopicNode> =
        initialTopics.associateByTo(LinkedHashMap()) { it.id }

    private val cards: MutableMap<String, Card> =
        initialCards.associateByTo(LinkedHashMap()) { it.id }

    private val reviews: MutableList<ReviewLog> = mutableListOf()

    override fun allTopics(): List<TopicNode> =
        topics.values.sortedWith(compareBy({ it.level.depth }, { it.order }, { it.name }))

    override fun childTopics(parentId: String?): List<TopicNode> =
        topics.values.filter { it.parentId == parentId }.sortedBy { it.order }

    override fun topicById(id: String): TopicNode? = topics[id]

    override fun upsertTopic(node: TopicNode) {
        val now = currentTimeMillis()
        val existing = topics[node.id]
        topics[node.id] = when {
            existing == null -> node.copy(createdAt = now, updatedAt = now)
            else -> node.copy(createdAt = existing.createdAt, updatedAt = now)
        }
    }

    override fun deleteTopic(id: String) {
        topics.remove(id)
    }

    override fun cardsByTopic(topicId: String): List<Card> =
        cards.values.filter { it.topicId == topicId && it.deletedAt == null }
            .sortedBy { it.createdAt }

    override fun cardsByTopics(topicIds: Collection<String>): List<Card> =
        cards.values.filter { it.topicId in topicIds && it.deletedAt == null }
            .sortedBy { it.createdAt }

    override fun cardById(id: String): Card? = cards[id]

    override fun upsertCard(card: Card) {
        val now = currentTimeMillis()
        val existing = cards[card.id]
        cards[card.id] = when {
            existing == null -> card.copy(createdAt = now, updatedAt = now)
            else -> card.copy(createdAt = existing.createdAt, updatedAt = now)
        }
    }

    override fun deleteCard(id: String) {
        val existing = cards[id] ?: return
        cards[id] = existing.copy(deletedAt = currentTimeMillis(), updatedAt = currentTimeMillis())
    }

    override fun insertReview(log: ReviewLog) {
        reviews += log
    }

    override fun reviewLogsByCard(cardId: String): List<ReviewLog> =
        reviews.filter { it.cardId == cardId }.sortedBy { it.reviewedAt }

    companion object {
        fun of(topics: List<TopicNode>, cards: List<Card>): InMemoryStore =
            InMemoryStore(topics, cards)

        /** Демо-хранилище на [SampleData]. */
        fun sample(): InMemoryStore = InMemoryStore(SampleData.topics(), SampleData.cards())
    }
}
