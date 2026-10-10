package com.elephantquizcards.data.db

import app.cash.sqldelight.db.SqlDriver
import com.elephantquizcards.data.db.Card as CardRow
import com.elephantquizcards.data.db.ReviewLog as ReviewLogRow
import com.elephantquizcards.data.db.Topic as TopicRow
import com.elephantquizcards.domain.model.Card
import com.elephantquizcards.domain.model.CardOption
import com.elephantquizcards.domain.model.CardSource
import com.elephantquizcards.domain.model.CardType
import com.elephantquizcards.domain.model.NodeLevel
import com.elephantquizcards.domain.model.QuestionVariant
import com.elephantquizcards.domain.model.ReviewLog
import com.elephantquizcards.domain.model.ReviewMode
import com.elephantquizcards.domain.model.TopicNode
import com.elephantquizcards.domain.repository.QuizStore
import com.elephantquizcards.platform.currentTimeMillis
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Реализация [QuizStore] поверх SQLDelight. Списки (options/variants/labels/tags)
 * хранятся в JSON-колонках — упрощение до нормализации схемы (см. DATA-MODEL).
 */
class SqlDelightStore(private val database: Database) : QuizStore {

    private constructor(driver: SqlDriver) : this(Database(driver))

    private val topicQueries get() = database.topicQueries
    private val cardQueries get() = database.cardQueries
    private val reviewLogQueries get() = database.reviewLogQueries

    // --- Topics ---

    override fun allTopics(): List<TopicNode> =
        topicQueries.selectAllTopics().executeAsList().map(::toDomain)

    override fun childTopics(parentId: String?): List<TopicNode> {
        val rows = if (parentId == null) {
            topicQueries.selectRootTopics().executeAsList()
        } else {
            topicQueries.selectChildTopics(parentId).executeAsList()
        }
        return rows.map(::toDomain)
    }

    override fun topicById(id: String): TopicNode? =
        topicQueries.selectTopicById(id).executeAsOneOrNull()?.let(::toDomain)

    override fun upsertTopic(node: TopicNode) {
        val now = currentTimeMillis()
        val existing = topicQueries.selectTopicById(node.id).executeAsOneOrNull()
        val createdAt = existing?.createdAt ?: node.createdAt
        val updatedAt = if (existing == null) node.updatedAt else now
        topicQueries.upsertTopic(
            id = node.id,
            parentId = node.parentId,
            level = node.level.name,
            name = node.name,
            sortOrder = node.order.toLong(),
            color = node.color,
            createdAt = createdAt,
            updatedAt = updatedAt,
            deletedAt = node.deletedAt,
            syncVersion = node.syncVersion,
        )
    }

    override fun deleteTopic(id: String) {
        topicQueries.deleteTopic(id)
    }

    // --- Cards ---

    override fun cardsByTopic(topicId: String): List<Card> =
        cardQueries.selectCardsByTopic(topicId).executeAsList().map(::toDomain)

    override fun cardsByTopics(topicIds: Collection<String>): List<Card> {
        if (topicIds.isEmpty()) return emptyList()
        return cardQueries.selectCardsByTopics(topicIds).executeAsList().map(::toDomain)
    }

    override fun cardById(id: String): Card? =
        cardQueries.selectCardById(id).executeAsOneOrNull()?.let(::toDomain)

    override fun upsertCard(card: Card) {
        val now = currentTimeMillis()
        val existing = cardQueries.selectCardById(card.id).executeAsOneOrNull()
        val createdAt = existing?.createdAt ?: card.createdAt
        val updatedAt = if (existing == null) card.updatedAt else now
        cardQueries.upsertCard(
            id = card.id,
            schemaVersion = card.schemaVersion.toLong(),
            syncVersion = card.syncVersion,
            createdAt = createdAt,
            updatedAt = updatedAt,
            deletedAt = card.deletedAt,
            type = card.type.name,
            question = card.question,
            questionCode = card.questionCode,
            answer = card.answer,
            answerCode = card.answerCode,
            explanation = card.explanation,
            optionsJson = json.encodeToString(card.options.map(::toDto)),
            variantsJson = json.encodeToString(card.variants.map(::toDto)),
            topicId = card.topicId,
            labelsJson = json.encodeToString(card.labels),
            tagsJson = json.encodeToString(card.tags),
            source = card.source.name,
            aiUnreviewed = if (card.aiUnreviewed) 1 else 0,
            favorite = if (card.favorite) 1 else 0,
            difficulty = card.difficulty?.toLong(),
            easinessFactor = card.easinessFactor,
            intervalDays = card.intervalDays.toLong(),
            repetitions = card.repetitions.toLong(),
            dueDate = card.dueDate,
            lastReviewedAt = card.lastReviewedAt,
            lapses = card.lapses.toLong(),
        )
    }

    override fun deleteCard(id: String) {
        val now = currentTimeMillis()
        cardQueries.softDeleteCard(deletedAt = now, updatedAt = now, id = id)
    }

    // --- Review log ---

    override fun insertReview(log: ReviewLog) {
        reviewLogQueries.insertReviewLog(
            id = log.id,
            cardId = log.cardId,
            variantId = log.variantId,
            reviewedAt = log.reviewedAt,
            mode = log.mode.name,
            quality = log.quality.toLong(),
            livesLost = log.livesLost?.toLong(),
            intervalBefore = log.intervalBefore.toLong(),
            intervalAfter = log.intervalAfter.toLong(),
            timeSpentMs = log.timeSpentMs,
        )
    }

    override fun reviewLogsByCard(cardId: String): List<ReviewLog> =
        reviewLogQueries.selectReviewLogsByCard(cardId).executeAsList().map(::toDomain)

    // --- Mappers ---

    private fun toDomain(row: TopicRow): TopicNode = TopicNode(
        id = row.id,
        parentId = row.parentId,
        level = NodeLevel.valueOf(row.level),
        name = row.name,
        order = row.sortOrder.toInt(),
        color = row.color,
        createdAt = row.createdAt,
        updatedAt = row.updatedAt,
        deletedAt = row.deletedAt,
        syncVersion = row.syncVersion,
    )

    private fun toDomain(row: CardRow): Card = Card(
        id = row.id,
        schemaVersion = row.schemaVersion.toInt(),
        syncVersion = row.syncVersion,
        createdAt = row.createdAt,
        updatedAt = row.updatedAt,
        deletedAt = row.deletedAt,
        type = CardType.valueOf(row.type),
        question = row.question,
        questionCode = row.questionCode,
        answer = row.answer,
        answerCode = row.answerCode,
        explanation = row.explanation,
        options = json.decodeFromString<List<CardOptionDto>>(row.optionsJson).map(::toDomain),
        variants = json.decodeFromString<List<QuestionVariantDto>>(row.variantsJson).map(::toDomain),
        topicId = row.topicId,
        labels = json.decodeFromString<List<String>>(row.labelsJson),
        tags = json.decodeFromString<List<String>>(row.tagsJson),
        source = CardSource.valueOf(row.source),
        aiUnreviewed = row.aiUnreviewed != 0L,
        favorite = row.favorite != 0L,
        difficulty = row.difficulty?.toInt(),
        easinessFactor = row.easinessFactor,
        intervalDays = row.intervalDays.toInt(),
        repetitions = row.repetitions.toInt(),
        dueDate = row.dueDate,
        lastReviewedAt = row.lastReviewedAt,
        lapses = row.lapses.toInt(),
    )

    private fun toDto(option: CardOption) = CardOptionDto(option.id, option.text, option.isCorrect, option.order)
    private fun toDomain(dto: CardOptionDto) = CardOption(dto.id, dto.text, dto.isCorrect, dto.order)
    private fun toDto(variant: QuestionVariant) = QuestionVariantDto(variant.id, variant.questionText, variant.questionCode)
    private fun toDomain(dto: QuestionVariantDto) = QuestionVariant(dto.id, dto.questionText, dto.questionCode)

    private fun toDomain(row: ReviewLogRow): ReviewLog = ReviewLog(
        id = row.id,
        cardId = row.cardId,
        variantId = row.variantId,
        reviewedAt = row.reviewedAt,
        mode = ReviewMode.valueOf(row.mode),
        quality = row.quality.toInt(),
        livesLost = row.livesLost?.toInt(),
        intervalBefore = row.intervalBefore.toInt(),
        intervalAfter = row.intervalAfter.toInt(),
        timeSpentMs = row.timeSpentMs,
    )

    companion object {
        private val json = Json { ignoreUnknownKeys = true }

        /** Открывает БД по платформенному драйверу. */
        fun open(driver: SqlDriver): SqlDelightStore = SqlDelightStore(driver)
    }
}

@Serializable
private data class CardOptionDto(
    val id: String,
    val text: String,
    val isCorrect: Boolean,
    val order: Int,
)

@Serializable
private data class QuestionVariantDto(
    val id: String,
    val questionText: String,
    val questionCode: String? = null,
)
