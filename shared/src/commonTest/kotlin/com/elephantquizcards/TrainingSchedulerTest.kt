package com.elephantquizcards

import com.elephantquizcards.data.local.InMemoryStore
import com.elephantquizcards.domain.logic.MILLIS_PER_DAY
import com.elephantquizcards.domain.logic.TrainingScheduler
import com.elephantquizcards.domain.model.ReviewMode
import com.elephantquizcards.domain.model.newManualCard
import kotlin.test.Test
import kotlin.test.assertEquals

class TrainingSchedulerTest {

    private val nowMillis = 1_000_000L

    @Test
    fun success_appliesSm2AndWritesReviewLog() {
        val store = InMemoryStore.of(emptyList(), emptyList())
        val card = newManualCard("t1", "Q", "A", now = nowMillis)
        store.upsertCard(card)
        val scheduler = TrainingScheduler(store, now = { 2L * nowMillis })

        val updated = scheduler.submit(card, ReviewMode.SELF_CONTROL, quality = 5)

        assertEquals(1, updated.intervalDays)
        assertEquals(1, updated.repetitions)
        assertEquals(0, updated.lapses)
        assertEquals(2L * nowMillis, updated.lastReviewedAt)
        assertEquals(2L * nowMillis + MILLIS_PER_DAY, updated.dueDate)
        assertEquals(updated.id, store.cardById(card.id)?.id)

        val log = store.reviewLogsByCard(card.id).single()
        assertEquals(ReviewMode.SELF_CONTROL, log.mode)
        assertEquals(5, log.quality)
        assertEquals(0, log.intervalBefore)
        assertEquals(1, log.intervalAfter)
        assertEquals(2L * nowMillis, log.reviewedAt)
        assertEquals(null, log.variantId)
    }

    @Test
    fun failure_resetsRepetitionsAndRaisesLapses() {
        val store = InMemoryStore.of(emptyList(), emptyList())
        val card = newManualCard("t1", "Q", "A", now = nowMillis)
            .copy(repetitions = 2, intervalDays = 6)
        store.upsertCard(card)
        val scheduler = TrainingScheduler(store, now = { 2L * nowMillis })

        val updated = scheduler.submit(card, ReviewMode.OPEN, quality = 1)

        assertEquals(0, updated.repetitions)
        assertEquals(1, updated.intervalDays)
        assertEquals(1, updated.lapses)
        assertEquals(1, store.reviewLogsByCard(card.id).single().intervalAfter)
        assertEquals(2L * nowMillis, updated.lastReviewedAt)
    }

    @Test
    fun twoSuccesses_growIntervalAndAppendLogs() {
        val store = InMemoryStore.of(emptyList(), emptyList())
        val card = newManualCard("t1", "Q", "A", now = nowMillis)
        store.upsertCard(card)
        val scheduler = TrainingScheduler(store, now = { 2L * nowMillis })

        val first = scheduler.submit(card, ReviewMode.SELF_CONTROL, quality = 5)
        val second = scheduler.submit(first, ReviewMode.SELF_CONTROL, quality = 5)

        assertEquals(6, second.intervalDays)
        assertEquals(2, second.repetitions)
        assertEquals(2, store.reviewLogsByCard(card.id).size)
    }
}