package com.elephantquizcards.domain.model

import com.benasher44.uuid.uuid4
import com.elephantquizcards.platform.currentTimeMillis

/** Тип карточки (§2.2 ТЗ). */
enum class CardType { SELF_CHECK, MULTIPLE_CHOICE }

/** Источник карточки (§2.2 ТЗ). */
enum class CardSource { MANUAL, AI_GENERATED, IMPORTED }

/** Вариант ответа для MULTIPLE_CHOICE (§2.2 ТЗ). */
data class CardOption(
    val id: String,
    val text: String,
    val isCorrect: Boolean,
    val order: Int,
)

/** Формулировка вопроса; их может быть несколько (≥1) (§2.2 ТЗ). */
data class QuestionVariant(
    val id: String,
    val questionText: String,
    val questionCode: String? = null,
)

/**
 * Карточка (§2.2 ТЗ).
 *
 * SM-2 поля хранятся на уровне карточки; худший вариант правит срок.
 * `deletedAt` — soft delete для синка.
 */
data class Card(
    val id: String,
    val schemaVersion: Int = CURRENT_SCHEMA_VERSION,
    val syncVersion: Long = 0,
    val createdAt: Long,
    val updatedAt: Long,
    val deletedAt: Long? = null,
    val type: CardType,
    val question: String,
    val questionCode: String? = null,
    val answer: String,
    val answerCode: String? = null,
    val explanation: String? = null,
    val options: List<CardOption> = emptyList(),
    val variants: List<QuestionVariant> = emptyList(),
    val topicId: String,
    val labels: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val source: CardSource = CardSource.MANUAL,
    val aiUnreviewed: Boolean = false,
    val favorite: Boolean = false,
    val difficulty: Int? = null,
    val easinessFactor: Double = DEFAULT_EASINESS_FACTOR,
    val intervalDays: Int = 0,
    val repetitions: Int = 0,
    val dueDate: Long,
    val lastReviewedAt: Long? = null,
    val lapses: Int = 0,
) {
    companion object {
        const val CURRENT_SCHEMA_VERSION: Int = 1
        const val DEFAULT_EASINESS_FACTOR: Double = 2.5
    }
}

/** Фабрика ручной карточки (source = MANUAL, старт SM-2 по умолчанию). */
fun newManualCard(
    topicId: String,
    question: String,
    answer: String,
    type: CardType = CardType.SELF_CHECK,
    now: Long = currentTimeMillis(),
): Card = Card(
    id = uuid4().toString(),
    createdAt = now,
    updatedAt = now,
    type = type,
    question = question,
    answer = answer,
    topicId = topicId,
    dueDate = now,
)
