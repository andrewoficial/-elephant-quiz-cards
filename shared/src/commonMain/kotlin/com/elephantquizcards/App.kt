package com.elephantquizcards

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.elephantquizcards.domain.logic.TrainingScheduler
import com.elephantquizcards.domain.repository.QuizStore
import com.elephantquizcards.presentation.editor.EditorScreen
import com.elephantquizcards.presentation.jungle.JungleScreen
import com.elephantquizcards.presentation.navigation.AppNavigator
import com.elephantquizcards.presentation.navigation.Screen
import com.elephantquizcards.presentation.subjects.SubjectsScreen
import com.elephantquizcards.presentation.training.TrainingScreen
import org.koin.compose.koinInject

@Composable
fun App() {
    MaterialTheme {
        val store = koinInject<QuizStore>()
        val scheduler = koinInject<TrainingScheduler>()
        val navigator = remember { AppNavigator() }

        when (val screen = navigator.current) {
            Screen.Subjects -> SubjectsScreen(
                store = store,
                onOpenArea = { navigator.navigate(Screen.Editor(it)) },
                onOpenJungle = { navigator.navigate(Screen.Jungle(it)) },
            )
            is Screen.Editor -> EditorScreen(
                store = store,
                topicId = screen.areaId,
                onBack = navigator::back,
            )
            is Screen.Jungle -> JungleScreen(
                store = store,
                areaId = screen.areaId,
                onBack = navigator::back,
                onStartTraining = { navigator.navigate(Screen.Training(screen.areaId, it)) },
            )
            is Screen.Training -> TrainingScreen(
                store = store,
                scheduler = scheduler,
                areaId = screen.areaId,
                mode = screen.mode,
                onBack = navigator::back,
            )
        }
    }
}