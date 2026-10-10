package com.elephantquizcards.di

import com.elephantquizcards.data.db.DatabaseDriverFactory
import org.koin.core.module.Module
import org.koin.dsl.module

/** iOS-привязки: драйвер БД на `NativeSqliteDriver` (на Windows не собирается). */
actual val platformModule: Module = module {
    single<DatabaseDriverFactory> { DatabaseDriverFactory() }
}