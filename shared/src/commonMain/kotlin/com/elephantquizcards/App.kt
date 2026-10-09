package com.elephantquizcards

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.elephantquizcards.data.db.DatabaseDriverFactory
import com.elephantquizcards.data.db.SqlDelightStore
import com.elephantquizcards.data.local.SampleData
import com.elephantquizcards.presentation.editor.EditorScreen
import com.elephantquizcards.presentation.subjects.SubjectsScreen

private sealed interface Screen {
    data object Subjects : Screen
    data class Editor(val topicId: String) : Screen
}

@Composable
fun App(driverFactory: DatabaseDriverFactory) {
    MaterialTheme {
        val store = remember {
            SqlDelightStore.open(driverFactory.createDriver()).also(SampleData::seedIfEmpty)
        }
        var screen by remember { mutableStateOf<Screen>(Screen.Subjects) }

        when (val current = screen) {
            Screen.Subjects -> SubjectsScreen(
                store = store,
                onOpenArea = { screen = Screen.Editor(it) },
            )
            is Screen.Editor -> EditorScreen(
                store = store,
                topicId = current.topicId,
                onBack = { screen = Screen.Subjects },
            )
        }
    }
}
