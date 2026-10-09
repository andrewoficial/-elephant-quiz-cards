package com.elephantquizcards.data.local

import com.elephantquizcards.domain.model.Card
import com.elephantquizcards.domain.model.NodeLevel
import com.elephantquizcards.domain.model.TopicNode
import com.elephantquizcards.domain.model.newManualCard
import com.elephantquizcards.domain.repository.QuizStore
import com.elephantquizcards.platform.currentTimeMillis

/** Демо-дерево и карточки; используется для превью и первичного наполнения БД. */
object SampleData {

    fun topics(now: Long = currentTimeMillis()): List<TopicNode> {
        fun topic(id: String, parentId: String?, level: NodeLevel, name: String, order: Int) =
            TopicNode(
                id = id,
                parentId = parentId,
                level = level,
                name = name,
                order = order,
                createdAt = now,
                updatedAt = now,
            )

        return listOf(
            topic("java", null, NodeLevel.DISCIPLINE, "Java", 0),
            topic("java-core", "java", NodeLevel.AREA, "Core (ООП, строки, исключения)", 0),
            topic("java-collections", "java", NodeLevel.AREA, "Collections", 1),
            topic("java-concurrency", "java", NodeLevel.AREA, "Concurrency", 2),
            topic("kotlin", null, NodeLevel.DISCIPLINE, "Kotlin", 1),
            topic("kotlin-basics", "kotlin", NodeLevel.AREA, "Основы и типы", 0),
            topic("sql", null, NodeLevel.DISCIPLINE, "SQL", 2),
            topic("sql-queries", "sql", NodeLevel.AREA, "Запросы, JOIN", 0),
        )
    }

    fun cards(now: Long = currentTimeMillis()): List<Card> = listOf(
        newManualCard(
            topicId = "java-collections",
            question = "Чем отличается ArrayList от LinkedList?",
            answer = "ArrayList — массив (быстрый доступ по индексу O(1)); " +
                "LinkedList — двусвязный список (быстрые вставки/удаления по краям O(1), " +
                "доступ по индексу O(n)).",
            now = now,
        ),
        newManualCard(
            topicId = "java-collections",
            question = "Как работает HashMap?",
            answer = "Хеш-таблица: массив бакетов + связные списки/красно-чёрные деревья (с Java 8). " +
                "Ключ → hashCode → индекс бакета; коллизии разрешаются цепочками.",
            now = now,
        ),
    )

    /** Наполняет хранилище демо-данными, если оно пустое. */
    fun seedIfEmpty(store: QuizStore) {
        if (store.allTopics().any { it.parentId == null }) return
        topics().forEach(store::upsertTopic)
        cards().forEach(store::upsertCard)
    }
}
