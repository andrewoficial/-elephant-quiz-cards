package com.elephantquizcards.domain.logic

import com.elephantquizcards.domain.model.Card
import kotlin.math.roundToInt

/** Состояние алгоритма SM-2, хранимое на карточке (§2.2 ТЗ). */
data class Sm2State(
    val easinessFactor: Double,
    val intervalDays: Int,
    val repetitions: Int,
    val lapses: Int,
)

/**
 * Алгоритм SM-2 (§3 ТЗ).
 *
 * Оценки: 1 — провал, 3 — успех с потерями, 5 — чистый.
 * Провалом считается `quality <= [FAIL_QUALITY]`.
 */
object Sm2 {
    const val MIN_EASINESS_FACTOR = 1.3
    const val DEFAULT_EASINESS_FACTOR = 2.5

    /** `quality` ниже или равно этому значению — провал. */
    const val FAIL_QUALITY = 2

    fun review(state: Sm2State, quality: Int): Sm2State {
        require(quality in 0..5) { "quality должен быть в 0..5, получено $quality" }

        val newEf = (state.easinessFactor +
            (0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02)))
            .coerceAtLeast(MIN_EASINESS_FACTOR)

        return if (quality <= FAIL_QUALITY) {
            state.copy(
                easinessFactor = newEf,
                repetitions = 0,
                intervalDays = 1,
                lapses = state.lapses + 1,
            )
        } else {
            val reps = state.repetitions + 1
            val interval = when (reps) {
                1 -> 1
                2 -> 6
                else -> (state.intervalDays * newEf).roundToInt().coerceAtLeast(1)
            }
            state.copy(easinessFactor = newEf, repetitions = reps, intervalDays = interval)
        }
    }
}

/** Снимок SM-2-полей карточки. */
fun Card.sm2State(): Sm2State =
    Sm2State(easinessFactor, intervalDays, repetitions, lapses)

/** Применяет результат SM-2 и пересчитывает срок следующего повторения. */
fun Card.withSm2(state: Sm2State, now: Long): Card = copy(
    easinessFactor = state.easinessFactor,
    intervalDays = state.intervalDays,
    repetitions = state.repetitions,
    lapses = state.lapses,
    lastReviewedAt = now,
    dueDate = now + state.intervalDays.toLong() * MILLIS_PER_DAY,
)

const val MILLIS_PER_DAY = 24L * 60 * 60 * 1000
