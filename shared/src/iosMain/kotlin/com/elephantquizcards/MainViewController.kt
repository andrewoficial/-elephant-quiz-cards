package com.elephantquizcards

import androidx.compose.ui.window.ComposeUIViewController
import com.elephantquizcards.data.db.DatabaseDriverFactory

fun MainViewController() = ComposeUIViewController { App(DatabaseDriverFactory()) }
