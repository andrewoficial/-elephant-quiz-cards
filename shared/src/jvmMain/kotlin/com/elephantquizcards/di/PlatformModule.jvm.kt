package com.elephantquizcards.di

import com.elephantquizcards.data.db.DatabaseDriverFactory
import org.koin.core.module.Module
import org.koin.dsl.module

/** JVM/Desktop-привязки: драйвер БД на файле `quizcards.db` (рабочий каталог). */
actual val platformModule: Module = module {
    single<DatabaseDriverFactory> { DatabaseDriverFactory() }
}