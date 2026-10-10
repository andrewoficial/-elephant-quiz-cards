package com.elephantquizcards.presentation.editor

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import com.elephantquizcards.domain.logic.search
import com.elephantquizcards.domain.model.Card
import com.elephantquizcards.domain.model.newManualCard
import com.elephantquizcards.domain.repository.QuizStore

@Composable
fun EditorScreen(
    store: QuizStore,
    topicId: String,
    onBack: () -> Unit,
) {
    val topic = store.topicById(topicId)

    var refresh by remember { mutableIntStateOf(0) }
    val cards = remember(refresh, topicId) { store.cardsByTopic(topicId) }

    // Форма: null → создаём новую, иначе редактируем существующую.
    var editingId by remember { mutableStateOf<String?>(null) }
    val editingCard = editingId?.let { id -> cards.find { it.id == id } }
    var question by remember { mutableStateOf("") }
    var answer by remember { mutableStateOf("") }

    var query by remember { mutableStateOf("") }
    var pendingDelete by remember { mutableStateOf<Card?>(null) }

    val visibleCards = cards.search(query)
    val canSave = question.isNotBlank() && answer.isNotBlank()

    fun resetForm() {
        editingId = null
        question = ""
        answer = ""
    }

    fun save() {
        val q = question.trim()
        val a = answer.trim()
        if (editingCard != null) {
            store.upsertCard(editingCard.copy(question = q, answer = a))
        } else {
            store.upsertCard(newManualCard(topicId, q, a))
        }
        resetForm()
        refresh++
    }

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
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("Поиск…") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(16.dp))

            Text(
                text = if (editingCard == null) "Новая карточка" else "Редактирование",
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(
                    onClick = ::save,
                    enabled = canSave,
                ) {
                    Text(if (editingCard == null) "Добавить карточку" else "Сохранить")
                }
                if (editingCard != null) {
                    Spacer(Modifier.size(8.dp))
                    OutlinedButton(onClick = ::resetForm) {
                        Text("Отмена")
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Text(
                text = "Карточки: ${visibleCards.size} из ${cards.size}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                visibleCards.forEach { card ->
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
                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.End) {
                                TextButton(
                                    onClick = {
                                        editingId = card.id
                                        question = card.question
                                        answer = card.answer
                                    },
                                ) {
                                    Text("Изменить")
                                }
                                TextButton(onClick = { pendingDelete = card }) {
                                    Text("Удалить", color = MaterialTheme.colorScheme.error)
                                }
                            }
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

    pendingDelete?.let { card ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Удалить карточку?") },
            text = { Text(card.question) },
            confirmButton = {
                TextButton(
                    onClick = {
                        store.deleteCard(card.id)
                        pendingDelete = null
                        if (editingId == card.id) resetForm()
                        refresh++
                    },
                ) {
                    Text("Удалить", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) {
                    Text("Отмена")
                }
            },
        )
    }
}