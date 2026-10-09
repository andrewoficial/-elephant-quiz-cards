package com.elephantquizcards

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.elephantquizcards.data.db.DatabaseDriverFactory

fun main() = application {
    val driverFactory = DatabaseDriverFactory()
    Window(
        onCloseRequest = ::exitApplication,
        title = "ElephantQuizCards",
    ) {
        App(driverFactory)
    }
}
