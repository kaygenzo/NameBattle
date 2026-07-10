package com.telen.namebattle.presentation.auth

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.telen.namebattle.R
import com.telen.namebattle.presentation.setThemedContent
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AuthScreenTest {

    @get:Rule
    val rule = createComposeRule()

    private fun twoParents() = listOf(
        ParentOption(parentIndex = 0, name = "Alice", shortlistCount = 5, listValidated = true),
        ParentOption(parentIndex = 1, name = "Bob", shortlistCount = 3, listValidated = false),
    )

    // region — loading

    @Test
    fun loading_state_shows_no_content() {
        rule.setThemedContent {
            AuthScreenContent(state = AuthUiState(isLoading = true))
        }
        rule.onNodeWithText("Qui se connecte ?").assertDoesNotExist()
    }

    // region — structure

    @Test
    fun shows_screen_title() {
        rule.setThemedContent {
            AuthScreenContent(state = AuthUiState(isLoading = false, parents = twoParents()))
        }
        rule.onNodeWithText("Connexion").assertIsDisplayed()
    }

    @Test
    fun shows_who_connects_label() {
        rule.setThemedContent {
            AuthScreenContent(state = AuthUiState(isLoading = false, parents = twoParents()))
        }
        rule.onNodeWithText("Qui se connecte ?").assertIsDisplayed()
    }

    @Test
    fun shows_parent_cards_with_names() {
        rule.setThemedContent {
            AuthScreenContent(state = AuthUiState(isLoading = false, parents = twoParents()))
        }
        rule.onNodeWithText("Alice").assertIsDisplayed()
        rule.onNodeWithText("Bob").assertIsDisplayed()
    }

    // region — parent selection

    @Test
    fun clicking_parent_card_triggers_callback_with_index() {
        var selectedIndex = -1
        rule.setThemedContent {
            AuthScreenContent(
                state = AuthUiState(isLoading = false, parents = twoParents()),
                onSelectParent = { selectedIndex = it },
            )
        }
        rule.onNodeWithText("Alice").performClick()
        assertTrue(selectedIndex == 0)
    }

    @Test
    fun password_field_visible_when_parent_selected() {
        rule.setThemedContent {
            AuthScreenContent(
                state = AuthUiState(
                    isLoading = false,
                    parents = twoParents(),
                    selectedParentIndex = 0,
                    password = "",
                )
            )
        }
        rule.onNodeWithText("Mot de passe", substring = true).assertIsDisplayed()
    }

    @Test
    fun password_field_not_visible_when_no_parent_selected() {
        rule.setThemedContent {
            AuthScreenContent(
                state = AuthUiState(
                    isLoading = false,
                    parents = twoParents(),
                    selectedParentIndex = null,
                )
            )
        }
        rule.onNodeWithText("Se connecter").assertDoesNotExist()
    }

    // region — sign in button state

    @Test
    fun sign_in_button_disabled_when_password_blank() {
        rule.setThemedContent {
            AuthScreenContent(
                state = AuthUiState(
                    isLoading = false,
                    parents = twoParents(),
                    selectedParentIndex = 0,
                    password = "",
                )
            )
        }
        rule.onNodeWithText("Se connecter").assertIsNotEnabled()
    }

    @Test
    fun sign_in_button_enabled_when_password_not_blank() {
        rule.setThemedContent {
            AuthScreenContent(
                state = AuthUiState(
                    isLoading = false,
                    parents = twoParents(),
                    selectedParentIndex = 0,
                    password = "secret",
                )
            )
        }
        rule.onNodeWithText("Se connecter").assertIsEnabled()
    }

    @Test
    fun sign_in_button_disabled_when_checking() {
        rule.setThemedContent {
            AuthScreenContent(
                state = AuthUiState(
                    isLoading = false,
                    parents = twoParents(),
                    selectedParentIndex = 0,
                    password = "secret",
                    isChecking = true,
                )
            )
        }
        rule.onNodeWithText("Vérification…").assertIsDisplayed()
        rule.onNodeWithText("Vérification…").assertIsNotEnabled()
    }

    @Test
    fun clicking_sign_in_triggers_submit_callback() {
        var submitted = false
        rule.setThemedContent {
            AuthScreenContent(
                state = AuthUiState(
                    isLoading = false,
                    parents = twoParents(),
                    selectedParentIndex = 0,
                    password = "secret",
                ),
                onSubmit = { submitted = true },
            )
        }
        rule.onNodeWithText("Se connecter").performClick()
        assertTrue(submitted)
    }

    // region — error message

    @Test
    fun error_message_shown_when_error_res_set() {
        rule.setThemedContent {
            AuthScreenContent(
                state = AuthUiState(
                    isLoading = false,
                    parents = twoParents(),
                    selectedParentIndex = 0,
                    password = "wrong",
                    errorRes = R.string.error_wrong_password,
                )
            )
        }
        rule.onNodeWithText("Mot de passe incorrect", substring = true).assertIsDisplayed()
    }

    // region — launch battle button

    @Test
    fun launch_battle_button_shown_when_can_start_and_no_parent_selected() {
        rule.setThemedContent {
            AuthScreenContent(
                state = AuthUiState(
                    isLoading = false,
                    parents = twoParents(),
                    selectedParentIndex = null,
                    canStartBattle = true,
                )
            )
        }
        rule.onNodeWithText("Lancer la bataille", substring = true).assertIsDisplayed()
    }

    @Test
    fun launch_battle_button_hidden_when_parent_selected() {
        rule.setThemedContent {
            AuthScreenContent(
                state = AuthUiState(
                    isLoading = false,
                    parents = twoParents(),
                    selectedParentIndex = 0,
                    canStartBattle = true,
                    password = "secret",
                )
            )
        }
        rule.onNodeWithText("Lancer la bataille", substring = true).assertDoesNotExist()
    }

    @Test
    fun clicking_launch_battle_triggers_callback() {
        var called = false
        rule.setThemedContent {
            AuthScreenContent(
                state = AuthUiState(
                    isLoading = false,
                    parents = twoParents(),
                    selectedParentIndex = null,
                    canStartBattle = true,
                ),
                onLaunchBattle = { called = true },
            )
        }
        rule.onNodeWithText("Lancer la bataille", substring = true).performClick()
        assertTrue(called)
    }
}
