package com.elephantquizcards.domain.logic

import com.elephantquizcards.domain.model.Card
import com.elephantquizcards.domain.model.CardType
import com.elephantquizcards.domain.model.ReviewLog
import com.elephantquizcards.domain.model.ReviewMode

/** Порог интервала для «выучено» (§4 ТЗ). */
const val LEARNED_INTERVAL_DAYS = 20

/** `quality` не ниже этого значения считается успехом (§3 ТЗ). */
const val SUCCESS_QUALITY = 3

/** Максимальная длина ответа, при которой доступен метод TYPING (§3 ТЗ). */
const val MAX_TYPING_ANSWER_LENGTH = 60

/**
 * Применимые формы повторения для карточки (§3, §9.4 ТЗ):
 * CHOICE — только MULTIPLE_CHOICE; TYPING — короткие ответы без кода;
 * OPEN и SELF_CONTROL — для всех.
 */
fun applicableModes(card: Card): List<ReviewMode> {
    val modes = mutableListOf<ReviewMode>()
    if (card.type == CardType.MULTIPLE_CHOICE) modes += ReviewMode.CHOICE
    if (isTypingEligible(card)) modes += ReviewMode.TYPING
    modes += ReviewMode.OPEN
    modes += ReviewMode.SELF_CONTROL
    return modes
}

/** Метод «ввод по буквам» доступен только для коротких ответов без кода (§3 ТЗ). */
fun isTypingEligible(card: Card): Boolean =
    card.questionCode.isNullOrBlank() &&
        card.answerCode.isNullOrBlank() &&
        card.answer.length <= MAX_TYPING_ANSWER_LENGTH

/**
 * Критерий «выучено» (§4 ТЗ).
 *
 * Карточка выучена, если:
 * 1. для КАЖДОГО варианта последний ответ в КАЖДОЙ применимой форме — верный, И
 * 2. [Card.intervalDays] >= [LEARNED_INTERVAL_DAYS].
 *
 * Если у карточки нет [Card.variants], считается, что у неё один неявный вариант
 * (`variantId == null`).
 */
fun isLearned(card: Card, logs: List<ReviewLog>): Boolean {
    if (card.intervalDays < LEARNED_INTERVAL_DAYS) return false

    val variantIds: List<String?> = card.variants.map { it.id }.ifEmpty { listOf(null) }
    val modes = applicableModes(card)

    return variantIds.all { variantId ->
        modes.all { mode ->
            val last = logs.asSequence()
                .filter { it.cardId == card.id && it.variantId == variantId && it.mode == mode }
                .maxByOrNull { it.reviewedAt }
            last != null && last.quality >= SUCCESS_QUALITY
        }
    }
}

/** Доля выученных карточек среди [cards]. */
fun learnedRatio(cards: List<Card>, logs: List<ReviewLog>): Float {
    if (cards.isEmpty()) return 0f
    return cards.count { isLearned(it, logs) }.toFloat() / cards.size
}
