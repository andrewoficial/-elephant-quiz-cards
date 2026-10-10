package com.elephantquizcards

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.elephantquizcards.domain.repository.QuizStore
import com.elephantquizcards.presentation.editor.EditorScreen
import com.elephantquizcards.presentation.navigation.AppNavigator
import com.elephantquizcards.presentation.navigation.Screen
import com.elephantquizcards.presentation.subjects.SubjectsScreen
import org.koin.compose.koinInject

@Composable
fun App() {
    MaterialTheme {
        val store = koinInject<QuizStore>()
        val navigator = remember { AppNavigator() }

        when (val screen = navigator.current) {
            Screen.Subjects -> SubjectsScreen(
                store = store,
                onOpenArea = { navigator.navigate(Screen.Editor(it)) },
            )
            is Screen.Editor -> EditorScreen(
                store = store,
                topicId = screen.areaId,
                onBack = navigator::back,
            )
        }
    }
}