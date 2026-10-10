package com.elephantquizcards.di

import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

/**
 * Единая точка старта Koin: собирает общий граф [appModule] и платформенные
 * привязки [platformModule]. Вызывается из точек входа до создания UI.
 *
 * @param config дополнительная настройка (например, регистрация Android `Context`).
 */
fun initKoin(config: KoinAppDeclaration = {}): KoinApplication =
    startKoin {
        config()
        modules(appModule, platformModule)
    }