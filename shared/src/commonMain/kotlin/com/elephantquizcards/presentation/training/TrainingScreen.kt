package com.elephantquizcards.presentation.training

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import com.elephantquizcards.domain.logic.normalizeAnswer
import com.elephantquizcards.domain.logic.todayQueue
import com.elephantquizcards.domain.model.Card
import com.elephantquizcards.domain.model.ReviewMode
import com.elephantquizcards.domain.repository.QuizStore
import com.elephantquizcards.platform.currentTimeMillis

private data class AnswerResult(val quality: Int, val message: String)

/** «Тренировка» — прогон очереди «сегодня» в выбранной форме (§6 ТЗ). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainingScreen(
    store: QuizStore,
    scheduler: TrainingScheduler,
    areaId: String,
    mode: ReviewMode,
    onBack: () -> Unit,
) {
    val area = store.topicById(areaId)
    val queue = remember(areaId, mode) { store.todayQueue(areaId, currentTimeMillis()) }
    var index by remember { mutableIntStateOf(0) }
    var flipped by remember { mutableStateOf(false) }
    var input by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<AnswerResult?>(null) }

    val card = queue.getOrNull(index)

    fun submitCard(quality: Int) {
        card?.let { scheduler.submit(it, mode, quality) }
        flipped = false
        input = ""
        result = null
        index++
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("${area?.name ?: "Тренировка"} · ${modeTitle(mode)}")
                },
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
                QuestionCard(card = card, flipped = flipped)
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
                    else -> UnderConstruction(mode = mode, onBack = onBack)
                }
            }
        }
    }
}

@Composable
private fun QuestionCard(card: Card, flipped: Boolean) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
            Text(
                text = card.question,
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
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp),
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
        Spacer(Modifier.height(12.dp))
        Button(onClick = onNext, modifier = Modifier.fillMaxWidth()) {
            Text("Дальше")
        }
    }
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

@Composable
private fun UnderConstruction(mode: ReviewMode, onBack: () -> Unit) {
    Text(
        text = "Метод «${modeTitle(mode)}» будет доступен позже.",
        style = MaterialTheme.typography.bodyMedium,
    )
    Spacer(Modifier.height(12.dp))
    Button(onClick = onBack) {
        Text("Назад")
    }
}

private fun checkOpen(card: Card, input: String): AnswerResult =
    if (normalizeAnswer(card.answer) == normalizeAnswer(input)) {
        AnswerResult(quality = 5, message = "Верно")
    } else {
        AnswerResult(quality = 1, message = "Неверно. Правильный ответ: ${card.answer}")
    }

private fun modeTitle(mode: ReviewMode): String = when (mode) {
    ReviewMode.CHOICE -> "Варианты ответа"
    ReviewMode.TYPING -> "Ввод по буквам"
    ReviewMode.OPEN -> "Открытый вопрос"
    ReviewMode.SELF_CONTROL -> "Самоконтроль"
}