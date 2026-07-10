package com.telen.namebattle.presentation.launch

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.telen.namebattle.presentation.setThemedContent
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LaunchScreenTest {

    @get:Rule
    val rule = createComposeRule()

    private fun readyState(parent2Name: String? = "Bob") = LaunchUiState(
        isLoading = false,
        parent1Name = "Alice",
        parent1Count = 8,
        parent2Name = parent2Name,
        parent2Count = 6,
        total = 14,
        targetFinalists = 3,
        roundsEstimate = 3,
        canStart = true,
        isStarting = false,
    )

    // region — structure

    @Test
    fun shows_screen_title() {
        rule.setThemedContent {
            LaunchScreenContent(state = readyState())
        }
        rule.onNodeWithText("Lancement").assertIsDisplayed()
    }

    @Test
    fun shows_ready_for_battle_text() {
        rule.setThemedContent {
            LaunchScreenContent(state = readyState())
        }
        rule.onNodeWithText("Prêts pour la bataille ?").assertIsDisplayed()
    }

    // region — parent stats

    @Test
    fun shows_parent_1_name_and_count() {
        rule.setThemedContent {
            LaunchScreenContent(state = readyState())
        }
        rule.onNodeWithText("Alice").assertIsDisplayed()
        rule.onNodeWithText("8").assertIsDisplayed()
    }

    @Test
    fun shows_parent_2_name_and_count_in_duo_mode() {
        rule.setThemedContent {
            LaunchScreenContent(state = readyState(parent2Name = "Bob"))
        }
        rule.onNodeWithText("Bob").assertIsDisplayed()
        rule.onNodeWithText("6").assertIsDisplayed()
    }

    @Test
    fun parent_2_stat_not_shown_in_solo_mode() {
        rule.setThemedContent {
            LaunchScreenContent(state = readyState(parent2Name = null))
        }
        rule.onNodeWithText("Bob").assertDoesNotExist()
    }

    // region — launch button

    @Test
    fun launch_button_enabled_when_can_start() {
        rule.setThemedContent {
            LaunchScreenContent(state = readyState().copy(canStart = true, isStarting = false))
        }
        rule.onNodeWithText("Lancer la bataille", substring = true).assertIsEnabled()
    }

    @Test
    fun launch_button_disabled_when_cannot_start() {
        rule.setThemedContent {
            LaunchScreenContent(state = readyState().copy(canStart = false))
        }
        rule.onNodeWithText("Lancer la bataille", substring = true).assertIsNotEnabled()
    }

    @Test
    fun launch_button_shows_preparing_when_starting() {
        rule.setThemedContent {
            LaunchScreenContent(state = readyState().copy(canStart = true, isStarting = true))
        }
        rule.onNodeWithText("Préparation…").assertIsDisplayed()
        rule.onNodeWithText("Préparation…").assertIsNotEnabled()
    }

    @Test
    fun clicking_launch_triggers_start_callback() {
        var called = false
        rule.setThemedContent {
            LaunchScreenContent(
                state = readyState().copy(canStart = true, isStarting = false),
                onStart = { called = true },
            )
        }
        rule.onNodeWithText("Lancer la bataille", substring = true).performClick()
        assertTrue(called)
    }

    // region — back

    @Test
    fun clicking_back_triggers_callback() {
        var called = false
        rule.setThemedContent {
            LaunchScreenContent(
                state = readyState(),
                onBack = { called = true },
            )
        }
        rule.onNodeWithContentDescription("Retour").performClick()
        assertTrue(called)
    }
}
