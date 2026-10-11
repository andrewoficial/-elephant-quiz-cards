package com.elephantquizcards.presentation.training

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.elephantquizcards.domain.logic.TrainingScheduler
import com.elephantquizcards.domain.logic.TypingProgress
import com.elephantquizcards.domain.logic.checkChoice
import com.elephantquizcards.domain.logic.normalizeAnswer
import com.elephantquizcards.domain.logic.randomQuestion
import com.elephantquizcards.domain.logic.todayQueue
import com.elephantquizcards.domain.model.Card
import com.elephantquizcards.domain.model.ReviewMode
import com.elephantquizcards.domain.repository.QuizStore
import com.elephantquizcards.platform.currentTimeMillis

private data class AnswerResult(val quality: Int, val message: String)

/** «Тренировка» — прогон очереди «сегодня» в выбранной форме (§6 ТЗ). */
@Composable
fun TrainingScreen(
    store: QuizStore,
    scheduler: TrainingScheduler,
    areaId: String,
    mode: ReviewMode,
    onBack: () -> Unit,
) {
    val area = store.topicById(areaId)
    val queue = remember(areaId, mode) { store.todayQueue(areaId, mode, currentTimeMillis()) }
    var index by remember { mutableIntStateOf(0) }
    val card = queue.getOrNull(index)

    // Состояние текущей карточки (сбрасывается при переходе к следующей).
    val shownQuestion = remember(card?.id) { card?.randomQuestion() ?: "" }
    var flipped by remember(card?.id) { mutableStateOf(false) }
    var input by remember(card?.id) { mutableStateOf("") }
    var result by remember(card?.id) { mutableStateOf<AnswerResult?>(null) }
    var selected by remember(card?.id) { mutableStateOf(emptySet<String>()) }
    var session by remember(card?.id) {
        mutableStateOf(TypingProgress(answer = normalizeAnswer(card?.answer.orEmpty())))
    }

    fun submitCard(quality: Int) {
        card?.let { scheduler.submit(it, mode, quality) }
        index++
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("${area?.name ?: "Тренировка"} · ${modeTitle(mode)}") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("\u2039", style = MaterialTheme.typography.titleLarge)
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (card == null) {
                EndState(finished = index > 0, onBack = onBack)
            } else {
                Text(
                    text = "Карточка ${index + 1} из ${queue.size}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(12.dp))
                QuestionCard(card = card, question = shownQuestion, flipped = flipped)
                Spacer(Modifier.height(16.dp))

                when (mode) {
                    ReviewMode.SELF_CONTROL -> SelfControlBody(
                        flipped = flipped,
                        onFlip = { flipped = true },
                        onQuality = ::submitCard,
                    )
                    ReviewMode.OPEN -> OpenBody(
                        input = input,
                        onInput = { input = it },
                        result = result,
                        onCheck = { result = checkOpen(card, input) },
                        onNext = { result?.let { submitCard(it.quality) } },
                    )
                    ReviewMode.CHOICE -> ChoiceBody(
                        card = card,
                        selected = selected,
                        onToggle = { id ->
                            selected = if (id in selected) selected - id else selected + id
                        },
                        result = result,
                        onCheck = { result = checkChoiceAnswer(card, selected) },
                        onNext = { result?.let { submitCard(it.quality) } },
                    )
                    ReviewMode.TYPING -> TypingBody(
                        card = card,
                        session = session,
                        onInput = { newValue ->
                            session = if (newValue.length > session.typed.length) {
                                session.press(newValue.last())
                            } else {
                                session.copy(typed = newValue)
                            }
                        },
                        onNext = ::submitCard,
                    )
                }
            }
        }
    }
}

@Composable
private fun QuestionCard(card: Card, question: String, flipped: Boolean) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
            Text(
                text = question,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
            )
            if (flipped) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = card.answer,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
    }
}

@Composable
private fun SelfControlBody(
    flipped: Boolean,
    onFlip: () -> Unit,
    onQuality: (Int) -> Unit,
) {
    if (!flipped) {
        Button(onClick = onFlip, modifier = Modifier.fillMaxWidth()) {
            Text("Показать ответ")
        }
    } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedButton(onClick = { onQuality(1) }, modifier = Modifier.weight(1f)) {
                Text("Не помню")
            }
            Button(onClick = { onQuality(5) }, modifier = Modifier.weight(1f)) {
                Text("Помню")
            }
        }
    }
}

@Composable
private fun OpenBody(
    input: String,
    onInput: (String) -> Unit,
    result: AnswerResult?,
    onCheck: () -> Unit,
    onNext: () -> Unit,
) {
    if (result == null) {
        OutlinedTextField(
            value = input,
            onValueChange = onInput,
            label = { Text("Ваш ответ") },
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(12.dp))
        Button(onClick = onCheck, enabled = input.isNotBlank(), modifier = Modifier.fillMaxWidth()) {
            Text("Проверить")
        }
    } else {
        Feedback(result)
        Spacer(Modifier.height(12.dp))
        Button(onClick = onNext, modifier = Modifier.fillMaxWidth()) {
            Text("Дальше")
        }
    }
}

@Composable
private fun ChoiceBody(
    card: Card,
    selected: Set<String>,
    onToggle: (String) -> Unit,
    result: AnswerResult?,
    onCheck: () -> Unit,
    onNext: () -> Unit,
) {
    if (result == null) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            card.options.sortedBy { it.order }.forEach { option ->
                val isSelected = option.id in selected
                Card(
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outlineVariant
                        },
                    ),
                    modifier = Modifier.fillMaxWidth().clickable { onToggle(option.id) },
                ) {
                    Text(
                        text = if (isSelected) "\u2713  ${option.text}" else option.text,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            Button(
                onClick = onCheck,
                enabled = selected.isNotEmpty(),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Проверить")
            }
        }
    } else {
        Feedback(result)
        Spacer(Modifier.height(12.dp))
        Button(onClick = onNext, modifier = Modifier.fillMaxWidth()) {
            Text("Дальше")
        }
    }
}

@Composable
private fun TypingBody(
    card: Card,
    session: TypingProgress,
    onInput: (String) -> Unit,
    onNext: (Int) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Жизни: ${session.livesLeft}",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        LetterTiles(session)

        when {
            session.done -> {
                Text(
                    text = "Верно",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                Button(onClick = { onNext(5) }, modifier = Modifier.fillMaxWidth()) {
                    Text("Дальше")
                }
            }
            session.failed -> {
                Text(
                    text = "Не вышло. Правильный ответ: ${card.answer}",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.error,
                )
                Button(onClick = { onNext(1) }, modifier = Modifier.fillMaxWidth()) {
                    Text("Дальше")
                }
            }
            else -> {
                OutlinedTextField(
                    value = session.typed,
                    onValueChange = onInput,
                    label = { Text("Печатайте по буквам") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun LetterTiles(session: TypingProgress) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        session.answer.forEachIndexed { i, _ ->
            val typed = session.typed.getOrNull(i)
            Surface(
                shape = RoundedCornerShape(6.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                color = if (typed != null) {
                    MaterialTheme.colorScheme.secondaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
            ) {
                Box(
                    modifier = Modifier.size(width = 26.dp, height = 32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = typed?.toString() ?: "",
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }
        }
    }
}

@Composable
private fun Feedback(result: AnswerResult) {
    Text(
        text = result.message,
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Center,
        color = if (result.quality >= 3) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.error
        },
    )
}

@Composable
private fun EndState(finished: Boolean, onBack: () -> Unit) {
    Text(
        text = if (finished) "Все карточки пройдены" else "Очередь пуста",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.SemiBold,
    )
    Spacer(Modifier.height(8.dp))
    Text(
        text = if (finished) {
            "Возвращайся, когда подойдёт срок повторения."
        } else {
            "Сейчас нечего повторять. Загляни позже или добавь карточки."
        },
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
    )
    Spacer(Modifier.height(16.dp))
    Button(onClick = onBack) {
        Text("Вернуться")
    }
}

private fun checkOpen(card: Card, input: String): AnswerResult =
    if (normalizeAnswer(card.answer) == normalizeAnswer(input)) {
        AnswerResult(quality = 5, message = "Верно")
    } else {
        AnswerResult(quality = 1, message = "Неверно. Правильный ответ: ${card.answer}")
    }

private fun checkChoiceAnswer(card: Card, selected: Set<String>): AnswerResult =
    if (card.checkChoice(selected)) {
        AnswerResult(quality = 5, message = "Верно")
    } else {
        val correct = card.options.filter { it.isCorrect }.joinToString(", ") { it.text }
        AnswerResult(quality = 1, message = "Неверно. Правильные варианты: $correct")
    }

private fun modeTitle(mode: ReviewMode): String = when (mode) {
    ReviewMode.CHOICE -> "Варианты ответа"
    ReviewMode.TYPING -> "Ввод по буквам"
    ReviewMode.OPEN -> "Открытый вопрос"
    ReviewMode.SELF_CONTROL -> "Самоконтроль"
}