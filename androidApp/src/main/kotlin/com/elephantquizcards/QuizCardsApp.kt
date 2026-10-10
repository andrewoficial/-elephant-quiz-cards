package com.elephantquizcards

import android.app.Application
import com.elephantquizcards.di.initKoin

/** Точка входа Android: поднимает Koin-граф до создания UI. */
class QuizCardsApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin(this)
    }
}