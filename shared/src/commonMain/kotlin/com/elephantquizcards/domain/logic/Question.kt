package com.elephantquizcards.domain.logic

import com.elephantquizcards.domain.model.Card
import kotlin.random.Random

/**
 * Текст вопроса для показа: случайный вариант из [Card.variants], если они заданы,
 * иначе [Card.question] (§3 ТЗ — «каждый показ — случайный вариант из variants»).
 */
fun Card.randomQuestion(random: Random = Random): String =
    if (variants.isEmpty()) question else variants.random(random).questionText