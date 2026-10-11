package com.elephantquizcards

import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.node.DrawModifierNode
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
        CompositionLocalProvider(
            LocalIndication provides NoIndication,
            LocalRippleConfiguration provides null,
        ) {
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
}

private object NoIndication : IndicationNodeFactory {
    override fun create(interactionSource: InteractionSource): DelegatableNode = NoIndicationNode()

    override fun hashCode(): Int = -1

    override fun equals(other: Any?): Boolean = other === this
}

private class NoIndicationNode : Modifier.Node(), DrawModifierNode {
    override fun ContentDrawScope.draw() = drawContent()
}