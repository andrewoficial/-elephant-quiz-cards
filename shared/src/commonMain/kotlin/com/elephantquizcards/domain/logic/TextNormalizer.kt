package com.elephantquizcards.domain.logic

/**
 * Адаптер нормализации ответов для метода «открытый вопрос» (§3, §9.3 ТЗ):
 * регистр, пробелы и знаки препинания не учитываются при сравнении.
 *
 * Удаляются все символы, кроме букв, цифр, пробелов, дефиса и апострофа;
 * серии пробелов схлопываются.
 */
fun normalizeAnswer(text: String): String =
    text.trim()
        .lowercase()
        .replace(Regex("[’\u2019]"), "'")
        .replace(Regex("[^\\p{L}\\p{N}\\s'\\-]"), "")
        .replace(Regex("\\s+"), " ")
        .trim()