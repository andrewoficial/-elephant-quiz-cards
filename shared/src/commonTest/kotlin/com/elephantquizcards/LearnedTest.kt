package com.elephantquizcards

import com.elephantquizcards.domain.logic.applicableModes
import com.elephantquizcards.domain.logic.isLearned
import com.elephantquizcards.domain.model.Card
import com.elephantquizcards.domain.model.CardType
import com.elephantquizcards.domain.model.QuestionVariant
import com.elephantquizcards.domain.model.ReviewLog
import com.elephantquizcards.domain.model.ReviewMode
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LearnedTest {

    private fun card(
        type: CardType = CardType.SELF_CHECK,
        intervalDays: Int = 20,
        answer: String = "Короткий ответ",
        answerCode: String? = null,
        questionCode: String? = null,
        variants: List<QuestionVariant> = emptyList(),
    ): Card = Card(
        id = "c1",
        createdAt = 0,
        updatedAt = 0,
        type = type,
        question = "Q",
        answer = answer,
        answerCode = answerCode,
        questionCode = questionCode,
        topicId = "t1",
        dueDate = 0,
        intervalDays = intervalDays,
        variants = variants,
    )

    private fun log(card: Card, mode: ReviewMode, quality: Int, at: Long, variantId: String? = null) =
        ReviewLog(
            id = "$variantId-${mode.name}-$at",
            cardId = card.id,
            variantId = variantId,
            reviewedAt = at,
            mode = mode,
            quality = quality,
            intervalBefore = 0,
            intervalAfter = 1,
        )

    @Test
    fun notLearned_beforeInterval() {
        assertFalse(isLearned(card(intervalDays = 19), emptyList()))
    }

    @Test
    fun notLearned_withoutLogs() {
        assertFalse(isLearned(card(), emptyList()))
    }

    @Test
    fun learned_whenAllApplicableModesCorrect() {
        val c = card()
        val logs = applicableModes(c).map { log(c, it, quality = 5, at = 1L) }
        assertTrue(isLearned(c, logs))
    }

    @Test
    fun notLearned_whenOneModeFailed() {
        val c = card()
        val logs = applicableModes(c).mapIndexed { i, mode ->
            log(c, mode, quality = if (i == 0) 1 else 5, at = 1L)
        }
        assertFalse(isLearned(c, logs))
    }

    @Test
    fun lastAnswerWins() {
        val c = card()
        val logs = buildList {
            applicableModes(c).forEach { mode ->
                add(log(c, mode, quality = 1, at = 1L))
                add(log(c, mode, quality = 5, at = 2L))
            }
        }
        assertTrue(isLearned(c, logs))
    }

    @Test
    fun choiceOnlyForMultipleChoice() {
        assertTrue(ReviewMode.CHOICE in applicableModes(card(type = CardType.MULTIPLE_CHOICE)))
        assertFalse(ReviewMode.CHOICE in applicableModes(card(type = CardType.SELF_CHECK)))
    }

    @Test
    fun typingUnavailableForCodeAnswers() {
        assertFalse(ReviewMode.TYPING in applicableModes(card(answerCode = "val x = 1")))

        val c = card(answerCode = "val x = 1")
        val logs = applicableModes(c).map { log(c, it, quality = 5, at = 1L) }
        assertTrue(isLearned(c, logs))
    }

    @Test
    fun allVariantsMustBeCorrect() {
        val c = card(
            variants = listOf(
                QuestionVariant("v1", "Q1"),
                QuestionVariant("v2", "Q2"),
            ),
        )
        val modes = applicableModes(c)
        val logs = buildList {
            modes.forEach { add(log(c, it, quality = 5, at = 1L, variantId = "v1")) }
            modes.forEach { add(log(c, it, quality = 5, at = 1L, variantId = "v2")) }
        }
        assertTrue(isLearned(c, logs))

        val missingV2 = logs.filterNot { it.variantId == "v2" }
        assertFalse(isLearned(c, missingV2))
    }
}
