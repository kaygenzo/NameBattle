package com.telen.namebattle.presentation.setup

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.telen.namebattle.domain.model.Gender
import com.telen.namebattle.presentation.setThemedContent
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SetupScreenTest {

    @get:Rule
    val rule = createComposeRule()

    private fun defaultState() = SetupUiState()

    // region — structure

    @Test
    fun shows_screen_title() {
        rule.setThemedContent {
            SetupScreenContent(state = defaultState())
        }
        rule.onNodeWithText("Nouvelle session").assertIsDisplayed()
    }

    @Test
    fun shows_gender_section() {
        rule.setThemedContent {
            SetupScreenContent(state = defaultState())
        }
        rule.onNodeWithText("Genre recherché").assertIsDisplayed()
    }

    @Test
    fun shows_parent_1_section() {
        rule.setThemedContent {
            SetupScreenContent(state = defaultState())
        }
        rule.onNodeWithText("Parent 1").assertIsDisplayed()
    }

    @Test
    fun shows_solo_mode_toggle() {
        rule.setThemedContent {
            SetupScreenContent(state = defaultState())
        }
        rule.onNodeWithText("Mode solo").assertIsDisplayed()
        rule.onNodeWithText("Un seul parent choisit").assertIsDisplayed()
    }

    // region — parent 2 visibility

    @Test
    fun parent_2_section_shown_when_not_solo() {
        rule.setThemedContent {
            SetupScreenContent(state = defaultState().copy(soloMode = false))
        }
        rule.onNodeWithText("Parent 2").assertIsDisplayed()
    }

    @Test
    fun parent_2_section_hidden_when_solo_mode_on() {
        rule.setThemedContent {
            SetupScreenContent(state = defaultState().copy(soloMode = true))
        }
        rule.onNodeWithText("Parent 2").assertDoesNotExist()
    }

    // region — validate button state

    @Test
    fun validate_button_disabled_when_cannot_lock() {
        rule.setThemedContent {
            SetupScreenContent(
                state = defaultState().copy(
                    soloMode = true,
                    parent1CanLock = false,
                    parent1Locked = false
                )
            )
        }
        rule.onNodeWithText("Valider ✓").assertIsNotEnabled()
    }

    @Test
    fun validate_button_enabled_when_can_lock() {
        rule.setThemedContent {
            SetupScreenContent(
                state = defaultState().copy(
                    soloMode = true,
                    parent1Name = "Alice",
                    parent1Password = "pass",
                    parent1Confirm = "pass",
                    parent1CanLock = true,
                    parent1PasswordsMatch = true,
                    parent1Locked = false,
                )
            )
        }
        rule.onNodeWithText("Valider ✓").assertIsEnabled()
    }

    @Test
    fun clicking_validate_triggers_lock_callback() {
        var called = false
        rule.setThemedContent {
            SetupScreenContent(
                state = defaultState().copy(
                    soloMode = true,
                    parent1Name = "Alice",
                    parent1Password = "pass",
                    parent1Confirm = "pass",
                    parent1CanLock = true,
                    parent1PasswordsMatch = true,
                    parent1Locked = false,
                ),
                onLockParent1 = { called = true },
            )
        }
        rule.onNodeWithText("Valider ✓").performClick()
        assertTrue(called)
    }

    // region — password mismatch

    @Test
    fun password_mismatch_error_shown_when_confirm_not_empty_and_mismatch() {
        rule.setThemedContent {
            SetupScreenContent(
                state = defaultState().copy(
                    parent1Password = "pass1",
                    parent1Confirm = "pass2",
                    parent1PasswordsMatch = false,
                    parent1Locked = false,
                )
            )
        }
        rule.onNodeWithText("Les mots de passe ne correspondent pas").assertIsDisplayed()
    }

    @Test
    fun password_mismatch_error_not_shown_when_confirm_empty() {
        rule.setThemedContent {
            SetupScreenContent(
                state = defaultState().copy(
                    parent1Password = "pass1",
                    parent1Confirm = "",
                    parent1PasswordsMatch = false,
                    parent1Locked = false,
                )
            )
        }
        rule.onNodeWithText("Les mots de passe ne correspondent pas").assertDoesNotExist()
    }

    // region — locked parent

    @Test
    fun locked_parent_hides_form_and_shows_name() {
        rule.setThemedContent {
            SetupScreenContent(
                state = defaultState().copy(
                    soloMode = true,
                    parent1Name = "Alice",
                    parent1Locked = true,
                )
            )
        }
        rule.onNodeWithText("Alice").assertIsDisplayed()
        rule.onNodeWithText("Valider ✓").assertDoesNotExist()
    }

    // region — gender selection

    @Test
    fun shows_boy_and_girl_gender_tabs() {
        rule.setThemedContent {
            SetupScreenContent(state = defaultState())
        }
        rule.onNodeWithText("Garçon").assertIsDisplayed()
        rule.onNodeWithText("Fille").assertIsDisplayed()
    }

    @Test
    fun clicking_gender_tab_triggers_callback() {
        var selectedGender: Gender? = null
        rule.setThemedContent {
            SetupScreenContent(
                state = defaultState().copy(gender = Gender.BOY),
                onGenderChange = { selectedGender = it },
            )
        }
        rule.onNodeWithText("Fille").performClick()
        assertTrue(selectedGender == Gender.GIRL)
    }
}
