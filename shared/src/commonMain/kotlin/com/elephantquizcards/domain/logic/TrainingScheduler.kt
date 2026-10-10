package com.elephantquizcards.domain.logic

import com.benasher44.uuid.uuid4
import com.elephantquizcards.domain.model.Card
import com.elephantquizcards.domain.model.ReviewLog
import com.elephantquizcards.domain.model.ReviewMode
import com.elephantquizcards.domain.repository.QuizStore
import com.elephantquizcards.platform.currentTimeMillis

/**
 * Обрабатывает один ответ карточки: применяет SM-2, сохраняет обновлённую карточку
 * и дописывает запись в журнал повторений (§2.3, §3 ТЗ).
 *
 * Используется экранами тренировки; от времени зависит через [now], что позволяет
 * писать детерминированные тесты.
 */
class TrainingScheduler(
    private val store: QuizStore,
    private val now: () -> Long = { currentTimeMillis() },
) {

    /**
     * Фиксирует ответ и возвращает обновлённую карточку.
     *
     * @param quality Оценка ответа: 1 — провал, 3 — успех с потерями, 5 — чистый (§3 ТЗ).
     * @param variantId Вариант вопроса; если у карточки есть [Card.variants] и не указан,
     *   берётся первый (для карточек без вариантов — `null`).
     * @param timeSpentMs Время ответа в мс (необязательно).
     */
    fun submit(
        card: Card,
        mode: ReviewMode,
        quality: Int,
        variantId: String? = card.variants.firstOrNull()?.id,
        timeSpentMs: Long? = null,
    ): Card {
        require(quality in 0..5) { "quality должен быть в 0..5, получено $quality" }
        val reviewedAt = now()

        val before = card.sm2State()
        val after = Sm2.review(before, quality)
        val updated = card.withSm2(after, reviewedAt)

        store.upsertCard(updated)
        store.insertReview(
            ReviewLog(
                id = uuid4().toString(),
                cardId = card.id,
                variantId = variantId,
                reviewedAt = reviewedAt,
                mode = mode,
                quality = quality,
                intervalBefore = before.intervalDays,
                intervalAfter = after.intervalDays,
                timeSpentMs = timeSpentMs,
            ),
        )
        return updated
    }
}