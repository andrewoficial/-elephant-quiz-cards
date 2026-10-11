package com.elephantquizcards

import com.elephantquizcards.domain.logic.TypingProgress
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TypingTest {

    private val target = "кварк"

    @Test
    fun correctSequence_finishesWithoutLivesLost() {
        val done = target.fold(TypingProgress(answer = target)) { acc, c -> acc.press(c) }
        assertTrue(done.done)
        assertEquals(3, done.livesLeft)
        assertFalse(done.failed)
    }

    @Test
    fun wrongChar_losesLifeAndKeepsPrefix() {
        val p = TypingProgress(answer = target).press('к').press('х')
        assertEquals("к", p.typed)
        assertEquals(2, p.livesLeft)
        assertFalse(p.done)
    }

    @Test
    fun threeWrongChars_fail() {
        var p = TypingProgress(answer = target)
        repeat(3) { p = p.press('х') }
        assertTrue(p.failed)
        // После провала ввод игнорируется.
        assertEquals(p, p.press('к'))
    }

    @Test
    fun backspace_removesLastAcceptedChar() {
        val p = TypingProgress(answer = target).press('к').press('в')
        assertEquals("к", p.backspace().typed)
        assertEquals("", TypingProgress(answer = target).backspace().typed)
    }
}