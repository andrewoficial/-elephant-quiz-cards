package com.elephantquizcards

import com.elephantquizcards.domain.logic.Sm2
import com.elephantquizcards.domain.logic.Sm2State
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class Sm2Test {

    private val fresh = Sm2State(
        easinessFactor = Sm2.DEFAULT_EASINESS_FACTOR,
        intervalDays = 0,
        repetitions = 0,
        lapses = 0,
    )

    @Test
    fun firstSuccess_intervalOneDay() {
        val r = Sm2.review(fresh, quality = 5)
        assertEquals(1, r.intervalDays)
        assertEquals(1, r.repetitions)
        assertEquals(0, r.lapses)
    }

    @Test
    fun secondSuccess_intervalSixDays() {
        val r = Sm2.review(Sm2.review(fresh, 5), quality = 5)
        assertEquals(6, r.intervalDays)
        assertEquals(2, r.repetitions)
    }

    @Test
    fun thirdSuccess_isLongerThanSixDays() {
        var s = Sm2.review(fresh, 5)
        s = Sm2.review(s, 5)
        s = Sm2.review(s, 5)
        assertTrue(s.intervalDays >= 6, "interval=${s.intervalDays}")
        assertEquals(3, s.repetitions)
    }

    @Test
    fun failure_resetsRepetitionsAndCountsLapse() {
        var s = Sm2.review(Sm2.review(fresh, 5), 5) // interval 6
        s = Sm2.review(s, quality = 1)
        assertEquals(0, s.repetitions)
        assertEquals(1, s.intervalDays)
        assertEquals(1, s.lapses)
    }

    @Test
    fun easinessFactor_neverBelowMin() {
        var s = fresh
        repeat(20) { s = Sm2.review(s, quality = 1) }
        assertEquals(Sm2.MIN_EASINESS_FACTOR, s.easinessFactor)
    }

    @Test
    fun qualityThree_isSuccess() {
        val r = Sm2.review(fresh, quality = 3)
        assertEquals(1, r.repetitions)
        assertEquals(0, r.lapses)
    }
}
