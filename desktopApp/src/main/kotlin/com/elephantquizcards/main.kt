package com.elephantquizcards

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.elephantquizcards.di.initKoin

fun main() = application {
    initKoin()
    Window(
        onCloseRequest = ::exitApplication,
        title = "ElephantQuizCards",
    ) {
        App()
    }
}