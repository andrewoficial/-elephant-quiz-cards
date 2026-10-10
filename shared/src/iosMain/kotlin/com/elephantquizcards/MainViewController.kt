package com.elephantquizcards

import androidx.compose.ui.window.ComposeUIViewController
import com.elephantquizcards.di.initKoin

fun MainViewController() = ComposeUIViewController {
    initKoin()
    App()
}