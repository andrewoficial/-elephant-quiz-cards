package com.elephantquizcards

import com.elephantquizcards.domain.logic.matches
import com.elephantquizcards.domain.logic.search
import com.elephantquizcards.domain.model.Card
import com.elephantquizcards.domain.model.CardType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CardSearchTest {

    private fun card(q: String, a: String) = Card(
        id = "c",
        createdAt = 1,
        updatedAt = 1,
        type = CardType.SELF_CHECK,
        question = q,
        answer = a,
        topicId = "t",
        dueDate = 1,
    )

    @Test
    fun blankQuery_matchesEverything() {
        val c = card("HashMap", "Хеш-таблица")
        assertTrue(c.matches(""))
        assertTrue(c.matches("   "))
    }

    @Test
    fun matchesQuestion_caseInsensitive() {
        assertTrue(card("ArrayList vs LinkedList", "Массив").matches("arraylist"))
        assertTrue(card("HashMap", "бакеты").matches("hashmap"))
    }

    @Test
    fun matchesAnswer_caseInsensitive() {
        assertTrue(card("Почему?", "Красно-чёрное дерево").matches("чёрное"))
    }

    @Test
    fun noMatch_returnsFalse() {
        assertFalse(card("HashMap", "бакеты").matches("StringBuilder"))
    }

    @Test
    fun search_filtersAndPreservesOrder() {
        val cards = listOf(
            card("ArrayList", "массив"),
            card("LinkedList", "список"),
            card("HashMap", "бакеты"),
        )
        assertEquals(
            listOf("ArrayList", "LinkedList"),
            cards.search("list").map { it.question },
        )
        assertEquals(emptyList(), cards.search("несуществующий запрос"))
    }
}