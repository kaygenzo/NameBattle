package com.telen.namebattle.presentation.results

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.telen.namebattle.presentation.setThemedContent
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ResultsScreenTest {

    @get:Rule
    val rule = createComposeRule()

    private fun stateWithFinalists(vararg names: String, isExporting: Boolean = false) =
        ResultsUiState(
            isLoading = false,
            finalists = names.toList(),
            roundsPlayed = 3,
            isExporting = isExporting,
        )

    // region — structure

    @Test
    fun shows_finalists_title() {
        rule.setThemedContent {
            ResultsScreenContent(state = stateWithFinalists("Alice", "Bob"))
        }
        rule.onNodeWithText("Vos finalistes !").assertIsDisplayed()
    }

    @Test
    fun shows_finalist_names() {
        rule.setThemedContent {
            ResultsScreenContent(state = stateWithFinalists("Alice", "Bob", "Charlie"))
        }
        rule.onNodeWithText("Alice").assertIsDisplayed()
        rule.onNodeWithText("Bob").assertIsDisplayed()
        rule.onNodeWithText("Charlie").assertIsDisplayed()
    }

    @Test
    fun shows_single_finalist() {
        rule.setThemedContent {
            ResultsScreenContent(state = stateWithFinalists("Alice"))
        }
        rule.onNodeWithText("Alice").assertIsDisplayed()
    }

    // region — action buttons

    @Test
    fun shows_export_pdf_button() {
        rule.setThemedContent {
            ResultsScreenContent(state = stateWithFinalists("Alice", "Bob"))
        }
        rule.onNodeWithText("Exporter en PDF", substring = true).assertIsDisplayed()
    }

    @Test
    fun export_button_shows_generating_when_exporting() {
        rule.setThemedContent {
            ResultsScreenContent(state = stateWithFinalists("Alice", "Bob", isExporting = true))
        }
        rule.onNodeWithText("Génération…").assertIsDisplayed()
    }

    @Test
    fun export_button_shows_normal_label_when_not_exporting() {
        rule.setThemedContent {
            ResultsScreenContent(state = stateWithFinalists("Alice", "Bob", isExporting = false))
        }
        rule.onNodeWithText("Exporter en PDF", substring = true).assertIsDisplayed()
        rule.onNodeWithText("Génération…").assertDoesNotExist()
    }

    @Test
    fun shows_replay_button() {
        rule.setThemedContent {
            ResultsScreenContent(state = stateWithFinalists("Alice", "Bob"))
        }
        rule.onNodeWithText("Rejouer", substring = true).assertIsDisplayed()
    }

    @Test
    fun shows_back_to_home_button() {
        rule.setThemedContent {
            ResultsScreenContent(state = stateWithFinalists("Alice", "Bob"))
        }
        rule.onNodeWithText("Retour à l'accueil", substring = true).assertIsDisplayed()
    }

    // region — callbacks

    @Test
    fun clicking_replay_triggers_on_replay_callback() {
        var called = false
        rule.setThemedContent {
            ResultsScreenContent(
                state = stateWithFinalists("Alice", "Bob"),
                onReplay = { called = true },
            )
        }
        rule.onNodeWithText("Rejouer", substring = true).performClick()
        assertTrue(called)
    }

    @Test
    fun clicking_back_to_home_triggers_on_new_session_callback() {
        var called = false
        rule.setThemedContent {
            ResultsScreenContent(
                state = stateWithFinalists("Alice", "Bob"),
                onNewSession = { called = true },
            )
        }
        rule.onNodeWithText("Retour à l'accueil", substring = true).performClick()
        assertTrue(called)
    }

    @Test
    fun clicking_export_pdf_triggers_callback() {
        var called = false
        rule.setThemedContent {
            ResultsScreenContent(
                state = stateWithFinalists("Alice", "Bob"),
                onExportPdf = { called = true },
            )
        }
        rule.onNodeWithText("Exporter en PDF", substring = true).performClick()
        assertTrue(called)
    }
}
