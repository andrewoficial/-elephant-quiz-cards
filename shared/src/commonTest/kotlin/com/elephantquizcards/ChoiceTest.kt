package com.elephantquizcards

import com.elephantquizcards.domain.logic.checkChoice
import com.elephantquizcards.domain.logic.randomQuestion
import com.elephantquizcards.domain.model.CardOption
import com.elephantquizcards.domain.model.CardType
import com.elephantquizcards.domain.model.QuestionVariant
import com.elephantquizcards.domain.model.newManualCard
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ChoiceTest {

    private fun choiceCard(vararg correct: String) = newManualCard(
        topicId = "t1",
        question = "Q?",
        answer = "A",
        type = CardType.MULTIPLE_CHOICE,
        now = 0,
    ).copy(
        options = listOf(
            CardOption("o1", "Первый", correct.contains("o1"), 0),
            CardOption("o2", "Второй", correct.contains("o2"), 1),
            CardOption("o3", "Третий", correct.contains("o3"), 2),
        ),
    )

    @Test
    fun singleCorrect_exactSelectionWins() {
        val card = choiceCard("o2")
        assertTrue(card.checkChoice(setOf("o2")))
        assertFalse(card.checkChoice(setOf("o1")))
        assertFalse(card.checkChoice(setOf("o1", "o2")))
        assertFalse(card.checkChoice(emptySet()))
    }

    @Test
    fun multipleCorrect_allOrNothing() {
        val card = choiceCard("o1", "o3")
        assertTrue(card.checkChoice(setOf("o1", "o3")))
        assertFalse(card.checkChoice(setOf("o1")))
        assertFalse(card.checkChoice(setOf("o1", "o2", "o3")))
    }

    @Test
    fun noCorrectOptions_neverWins() {
        assertFalse(choiceCard().checkChoice(emptySet()))
        assertFalse(choiceCard().checkChoice(setOf("o1", "o2", "o3")))
    }

    @Test
    fun randomQuestion_withoutVariants_usesField() {
        assertEquals("Q?", choiceCard("o1").randomQuestion())
    }

    @Test
    fun randomQuestion_withVariants_picksDeterministicallyForSeed() {
        val card = choiceCard("o1").copy(
            variants = listOf(
                QuestionVariant("v1", "Вариант 1"),
                QuestionVariant("v2", "Вариант 2"),
            ),
        )
        assertEquals(card.randomQuestion(Random(42)), card.randomQuestion(Random(42)))
        assertTrue(card.randomQuestion(Random(42)) in setOf("Вариант 1", "Вариант 2"))
    }
}