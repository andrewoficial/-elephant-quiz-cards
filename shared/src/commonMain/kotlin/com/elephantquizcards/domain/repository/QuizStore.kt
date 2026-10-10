package com.elephantquizcards.domain.repository

/** Объединяет доступ к дереву тем, карточкам и журналу повторений. */
interface QuizStore : TopicRepository, CardRepository, ReviewLogRepository
