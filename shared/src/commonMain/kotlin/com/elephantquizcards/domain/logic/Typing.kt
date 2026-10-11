package com.elephantquizcards.domain.logic

/** Число жизней на один ответ в методе «ввод по буквам» (§3 ТЗ). */
const val MAX_TYPING_LIVES = 3

/**
 * Посимвольная проверка ответа методом «ввод по буквам» (§3, §9.3 ТЗ).
 *
 * Неверная буква = −1 жизнь; при 3 потерянных жизнях ответ не засчитывается.
 * В поле [typed] накапливаются только принятые (верные) буквы.
 */
data class TypingProgress(
    val answer: String,
    val typed: String = "",
    val livesLost: Int = 0,
) {
    val livesLeft: Int get() = MAX_TYPING_LIVES - livesLost

    /** Ответ набран полностью и без ошибок. */
    val done: Boolean get() = typed == answer

    /** Жизни кончились, а ответ не набран. */
    val failed: Boolean get() = !done && livesLost >= MAX_TYPING_LIVES

    /** Обработка введённой буквы: совпала — принимается, иначе теряется жизнь. */
    fun press(char: Char): TypingProgress {
        if (done || failed) return this
        val expected = answer[typed.length]
        return if (char == expected) copy(typed = typed + char) else copy(livesLost = livesLost + 1)
    }

    /** Удаление последней принятой буквы. */
    fun backspace(): TypingProgress =
        if (typed.isEmpty()) this else copy(typed = typed.dropLast(1))
}