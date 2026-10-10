package com.elephantquizcards.di

import com.elephantquizcards.data.db.DatabaseDriverFactory
import com.elephantquizcards.domain.repository.QuizStore
import kotlin.test.Test
import kotlin.test.assertTrue
import org.koin.dsl.koinApplication
import org.koin.dsl.module

/** Смок-проверка Koin-графа: `QuizStore` резолвится и наполняется демо-данными. */
class KoinModuleTest {

    @Test
    fun graph_resolvesQuizStoreWithSeedData() {
        val app = koinApplication {
            modules(
                appModule,
                module {
                    // В тесте — in-memory БД, чтобы не трогать файл quizcards.db.
                    single<DatabaseDriverFactory> { DatabaseDriverFactory(":memory:") }
                },
            )
        }.koin

        val store = app.get<QuizStore>()
        assertTrue(store.allTopics().any { it.parentId == null })
    }
}