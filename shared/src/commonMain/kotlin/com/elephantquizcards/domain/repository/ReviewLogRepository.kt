package com.elephantquizcards.domain.repository

import com.elephantquizcards.domain.model.ReviewLog

/**
 * Журнал повторений — append-only, синхронизируется целиком (§2.3 ТЗ).
 */
interface ReviewLogRepository {
    fun insertReview(log: ReviewLog)

    fun reviewLogsByCard(cardId: String): List<ReviewLog>
}