package com.elephantquizcards.domain.model

/** Форма повторения (§2.3, §3 ТЗ). */
enum class ReviewMode { CHOICE, TYPING, OPEN, SELF_CONTROL }

/** Пользовательское состояние карточки (§2.4 ТЗ). */
enum class CardState { NEW, LEARNING, REVIEW, ARCHIVED, SUSPENDED }

/**
 * Журнал повторений — append-only, синхронизируется целиком (§2.3 ТЗ).
 *
 * `quality`: 1 — провал, 3 — успех с потерями, 5 — чистый.
 */
data class ReviewLog(
    val id: String,
    val cardId: String,
    val variantId: String?,
    val reviewedAt: Long,
    val mode: ReviewMode,
    val quality: Int,
    val livesLost: Int? = null,
    val intervalBefore: Int,
    val intervalAfter: Int,
    val timeSpentMs: Long? = null,
)
