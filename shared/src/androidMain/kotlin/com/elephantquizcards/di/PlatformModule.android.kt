package com.elephantquizcards.di

import android.content.Context
import com.elephantquizcards.data.db.DatabaseDriverFactory
import org.koin.android.ext.koin.androidContext
import org.koin.core.KoinApplication
import org.koin.core.module.Module
import org.koin.dsl.module

/** Android-привязки: драйвер БД создаётся с `Context` из Koin-графа. */
actual val platformModule: Module = module {
    single<DatabaseDriverFactory> { DatabaseDriverFactory(androidContext()) }
}

/** Android-старт Koin: регистрирует `Context` приложения в графе. */
fun initKoin(context: Context): KoinApplication = initKoin { androidContext(context) }