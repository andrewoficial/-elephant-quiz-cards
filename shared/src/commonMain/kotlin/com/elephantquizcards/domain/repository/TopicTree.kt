package com.elephantquizcards.domain.repository

import com.elephantquizcards.domain.model.Card

/** id узла и всех его потомков (для агрегации прогресса/карточек). */
fun TopicRepository.subtreeTopicIds(rootId: String): Set<String> {
    val result = LinkedHashSet<String>()
    fun visit(id: String) {
        if (!result.add(id)) return
        childTopics(id).forEach { visit(it.id) }
    }
    visit(rootId)
    return result
}

/** Все карточки поддерева `rootId`. */
fun QuizStore.cardsInSubtree(rootId: String): List<Card> =
    cardsByTopics(subtreeTopicIds(rootId))
