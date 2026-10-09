package com.elephantquizcards.domain.repository

import com.elephantquizcards.domain.model.TopicNode

/** Доступ к дереву тем (§2.1 ТЗ). */
interface TopicRepository {
    fun allTopics(): List<TopicNode>

    fun childTopics(parentId: String?): List<TopicNode>

    fun topicById(id: String): TopicNode?

    /** Обновляет узел, если он есть, иначе добавляет. Также обновляет `updatedAt`. */
    fun upsertTopic(node: TopicNode)

    fun deleteTopic(id: String)
}
