package com.elephantquizcards.di

import com.elephantquizcards.data.db.DatabaseDriverFactory
import com.elephantquizcards.data.db.SqlDelightStore
import com.elephantquizcards.data.local.SampleData
import com.elephantquizcards.domain.repository.QuizStore
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Платформо-независимые зависимости приложения.
 *
 * `QuizStore` собирается поверх платформенного [DatabaseDriverFactory]
 * (регистрируется в [platformModule]) и при первом создании наполняется
 * демо-данными ([SampleData.seedIfEmpty]).
 */
val appModule: Module = module {
    single<QuizStore> {
        SqlDelightStore.open(get<DatabaseDriverFactory>().createDriver())
            .also(SampleData::seedIfEmpty)
    }
}
