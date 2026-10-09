package com.elephantquizcards.domain.repository

import com.elephantquizcards.domain.model.Card

/** Доступ к карточкам (§2.2 ТЗ). */
interface CardRepository {
    fun cardsByTopic(topicId: String): List<Card>

    fun cardsByTopics(topicIds: Collection<String>): List<Card>

    fun cardById(id: String): Card?

    /** Обновляет карточку, если она есть, иначе добавляет. Также обновляет `updatedAt`. */
    fun upsertCard(card: Card)

    fun deleteCard(id: String)
}
