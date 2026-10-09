package com.elephantquizcards.domain.model

/**
 * Уровень узла дерева тем (§2.1 ТЗ).
 *
 * 0 Дисциплина → 1 Область → 2 Тема → 3 Раздел → 4 Секция.
 */
enum class NodeLevel(val depth: Int) {
    DISCIPLINE(0),
    AREA(1),
    TOPIC(2),
    SECTION(3),
    SUBSECTION(4);

    /** Следующий уровень вложенности или `null`, если это лист. */
    val child: NodeLevel? get() = entries.getOrNull(ordinal + 1)
}

/**
 * Узел дерева тем (§2.1 ТЗ). Одна таблица-дерево.
 *
 * `parentId == null` только у [NodeLevel.DISCIPLINE]; родитель обязан иметь `level - 1`
 * (контроль в репозитории). Полный путь в карточке не хранится — только `topicId`.
 */
data class TopicNode(
    val id: String,
    val parentId: String?,
    val level: NodeLevel,
    val name: String,
    val order: Int = 0,
    val color: Long? = null,
    val createdAt: Long,
    val updatedAt: Long,
    val deletedAt: Long? = null,
    val syncVersion: Long = 0,
)
