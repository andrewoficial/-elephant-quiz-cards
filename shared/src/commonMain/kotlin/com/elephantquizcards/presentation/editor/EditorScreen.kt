package com.elephantquizcards.presentation.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.elephantquizcards.domain.model.newManualCard
import com.elephantquizcards.domain.repository.QuizStore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    store: QuizStore,
    topicId: String,
    onBack: () -> Unit,
) {
    val topic = store.topicById(topicId)

    var question by remember { mutableStateOf("") }
    var answer by remember { mutableStateOf("") }
    var refresh by remember { mutableIntStateOf(0) }

    val cards = remember(refresh, topicId) { store.cardsByTopic(topicId) }
    val canAdd = question.isNotBlank() && answer.isNotBlank()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(topic?.name ?: "Карточки") },
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
                text = "Новая карточка",
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = question,
                onValueChange = { question = it },
                label = { Text("Вопрос") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 1,
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = answer,
                onValueChange = { answer = it },
                label = { Text("Ответ") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
            )
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = {
                    store.upsertCard(newManualCard(topicId, question.trim(), answer.trim()))
                    question = ""
                    answer = ""
                    refresh++
                },
                enabled = canAdd,
            ) {
                Text("Добавить карточку")
            }

            Spacer(Modifier.height(24.dp))
            Text(
                text = "Карточки: ${cards.size}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                cards.forEach { card ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = card.question,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = card.answer,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
                if (cards.isEmpty()) {
                    Text(
                        text = "Пока пусто. Добавь первую карточку выше.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
