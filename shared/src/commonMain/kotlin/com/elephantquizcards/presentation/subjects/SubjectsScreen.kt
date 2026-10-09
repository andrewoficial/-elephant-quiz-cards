package com.elephantquizcards.presentation.subjects

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.elephantquizcards.data.local.InMemoryStore
import com.elephantquizcards.domain.model.NodeLevel
import com.elephantquizcards.domain.model.TopicNode
import com.elephantquizcards.domain.repository.QuizStore
import com.elephantquizcards.domain.repository.cardsInSubtree
import kotlin.math.roundToInt

private const val LEARNED_INTERVAL_DAYS = 20

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectsScreen(
    store: QuizStore,
    onOpenArea: (String) -> Unit,
) {
    val disciplines = store.childTopics(null).filter { it.level == NodeLevel.DISCIPLINE }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Elephant Quiz Cards") }) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(
                    text = "Направления",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            items(disciplines, key = { it.id }) { discipline ->
                val areas = store.childTopics(discipline.id)
                    .filter { it.level == NodeLevel.AREA }
                DisciplineCard(
                    discipline = discipline,
                    areas = areas,
                    cardsInSubtree = { store.cardsInSubtree(it) },
                    onOpenArea = onOpenArea,
                )
            }
        }
    }
}

@Composable
private fun DisciplineCard(
    discipline: TopicNode,
    areas: List<TopicNode>,
    cardsInSubtree: (String) -> List<com.elephantquizcards.domain.model.Card>,
    onOpenArea: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val allCards = areas.flatMap { cardsInSubtree(it.id) }
    val progress = learnedRatio(allCards)

    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = if (expanded) "▾" else "▸",
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = discipline.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(Modifier.height(8.dp))
                    ProgressLine(progress)
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    areas.forEach { area ->
                        AreaRow(
                            area = area,
                            cards = cardsInSubtree(area.id),
                            onClick = { onOpenArea(area.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AreaRow(
    area: TopicNode,
    cards: List<com.elephantquizcards.domain.model.Card>,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = area.name,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "${cards.size} карт. · ${(learnedRatio(cards) * 100).roundToInt()}%",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.height(4.dp))
        ProgressBar(learnedRatio(cards))
    }
}

@Composable
private fun ProgressLine(progress: Float) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        ProgressBar(progress, modifier = Modifier.weight(1f))
        Spacer(Modifier.width(8.dp))
        Text(
            text = "${(progress * 100).roundToInt()}%",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ProgressBar(progress: Float, modifier: Modifier = Modifier) {
    LinearProgressIndicator(
        progress = { progress.coerceIn(0f, 1f) },
        modifier = modifier.fillMaxWidth().height(8.dp),
        strokeCap = StrokeCap.Round,
    )
}

private fun learnedRatio(cards: List<com.elephantquizcards.domain.model.Card>): Float {
    if (cards.isEmpty()) return 0f
    val learned = cards.count { it.intervalDays >= LEARNED_INTERVAL_DAYS }
    return learned.toFloat() / cards.size
}

@Preview
@Composable
private fun SubjectsScreenPreview() {
    MaterialTheme { SubjectsScreen(store = InMemoryStore.sample(), onOpenArea = {}) }
}
