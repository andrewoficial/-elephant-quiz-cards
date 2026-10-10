package com.elephantquizcards

import com.elephantquizcards.domain.logic.normalizeAnswer
import kotlin.test.Test
import kotlin.test.assertEquals

class TextNormalizerTest {

    @Test
    fun ignoresCaseAndOuterWhitespace() {
        assertEquals("красное-чёрное", normalizeAnswer("  Красное-Чёрное "))
    }

    @Test
    fun stripsPunctuationAndCollapsesSpaces() {
        assertEquals("hello world", normalizeAnswer("Hello,   World!"))
        assertEquals("arraylist", normalizeAnswer("ArrayList."))
        assertEquals("a b c", normalizeAnswer("a, b; c:"))
    }

    @Test
    fun keepsDigitsHyphenAndApostrophe() {
        assertEquals("x86 процессор", normalizeAnswer("  X86, процессор! "))
        assertEquals("don't", normalizeAnswer("Don’t."))
    }
}