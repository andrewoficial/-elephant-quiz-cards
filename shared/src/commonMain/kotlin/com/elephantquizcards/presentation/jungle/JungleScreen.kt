package com.elephantquizcards.presentation.jungle

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.elephantquizcards.domain.model.CardType
import com.elephantquizcards.domain.model.ReviewMode
import com.elephantquizcards.domain.repository.QuizStore
import com.elephantquizcards.domain.logic.dueNow
import com.elephantquizcards.domain.logic.isTypingEligible
import com.elephantquizcards.domain.repository.cardsInSubtree
import com.elephantquizcards.platform.currentTimeMillis

/**
 * «Джунгли» — выбор формы повторения для области и очередь на сегодня (§6 ТЗ).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JungleScreen(
    store: QuizStore,
    areaId: String,
    onBack: () -> Unit,
    onStartTraining: (ReviewMode) -> Unit,
) {
    val area = store.topicById(areaId)
    val queue = remember(areaId) { store.cardsInSubtree(areaId).dueNow(currentTimeMillis()) }
    val choiceCount = queue.count { it.type == CardType.MULTIPLE_CHOICE }
    val typingCount = queue.count { isTypingEligible(it) }
    val hasQueue = queue.isNotEmpty()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(area?.name ?: "Джунгли") },
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
        ) {
            Text(
                text = "Джунгли",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Сегодня на очереди: ${queue.size} карточек",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(16.dp))

            MethodRow(
                name = "Самоконтроль",
                subtitle = "Переворот карточки: «помню / не помню»",
                enabled = hasQueue,
                onClick = { onStartTraining(ReviewMode.SELF_CONTROL) },
            )
            MethodRow(
                name = "Открытый вопрос",
                subtitle = "Свободный ввод ответа и проверка",
                enabled = hasQueue,
                onClick = { onStartTraining(ReviewMode.OPEN) },
            )
            MethodRow(
                name = "Варианты ответа",
                subtitle = "Подойдёт ${choiceCount} карточкам",
                enabled = false,
                soon = true,
                onClick = { onStartTraining(ReviewMode.CHOICE) },
            )
            MethodRow(
                name = "Ввод по буквам",
                subtitle = "Подойдёт ${typingCount} карточкам",
                enabled = false,
                soon = true,
                onClick = { onStartTraining(ReviewMode.TYPING) },
            )
        }
    }
}

@Composable
private fun MethodRow(
    name: String,
    subtitle: String,
    enabled: Boolean,
    soon: Boolean = false,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled && !soon, onClick = onClick),
        shape = RoundedCornerShape(12.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .alpha(if (enabled && !soon) 1f else 0.6f),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (soon) {
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "скорее",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}