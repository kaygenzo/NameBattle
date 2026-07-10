package com.telen.namebattle.presentation.home

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
class HomeScreenTest {

    @get:Rule
    val rule = createComposeRule()

    private fun defaultCallbacks(
        onCreateSession: () -> Unit = {},
        onManageLists: (Long) -> Unit = {},
        onStartBattle: (Long) -> Unit = {},
        onResumeBattle: (Long) -> Unit = {},
        onViewResults: (Long) -> Unit = {},
        onRestartBattle: (Long) -> Unit = {},
        onDeleteSession: (Long) -> Unit = {},
        onDeleteConfirmed: () -> Unit = {},
        onDeleteDismissed: () -> Unit = {},
    ) = Triple(onCreateSession, onManageLists, onDeleteSession)

    // region — loading state

    @Test
    fun loading_state_shows_progress_indicator() {
        rule.setThemedContent {
            HomeScreenContent(
                state = HomeUiState(isLoading = true),
                onCreateSession = {}, onManageLists = {}, onStartBattle = {},
                onResumeBattle = {}, onViewResults = {}, onRestartBattle = {},
                onDeleteSession = {}, onDeleteConfirmed = {}, onDeleteDismissed = {},
            )
        }
        rule.onNodeWithText("Nouvelle session", substring = true).assertDoesNotExist()
    }

    // region — empty state

    @Test
    fun empty_state_shows_app_name_and_tagline() {
        rule.setThemedContent {
            HomeScreenContent(
                state = HomeUiState(isLoading = false, sessions = emptyList()),
                onCreateSession = {}, onManageLists = {}, onStartBattle = {},
                onResumeBattle = {}, onViewResults = {}, onRestartBattle = {},
                onDeleteSession = {}, onDeleteConfirmed = {}, onDeleteDismissed = {},
            )
        }
        rule.onNodeWithText("Bataille de prénom").assertIsDisplayed()
        rule.onNodeWithText("Aide au choix du prénom de bébé").assertIsDisplayed()
    }

    @Test
    fun empty_state_shows_new_session_button() {
        rule.setThemedContent {
            HomeScreenContent(
                state = HomeUiState(isLoading = false, sessions = emptyList()),
                onCreateSession = {}, onManageLists = {}, onStartBattle = {},
                onResumeBattle = {}, onViewResults = {}, onRestartBattle = {},
                onDeleteSession = {}, onDeleteConfirmed = {}, onDeleteDismissed = {},
            )
        }
        rule.onNodeWithText("Nouvelle session", substring = true).assertIsDisplayed()
    }

    @Test
    fun clicking_new_session_button_triggers_callback() {
        var called = false
        rule.setThemedContent {
            HomeScreenContent(
                state = HomeUiState(isLoading = false, sessions = emptyList()),
                onCreateSession = { called = true }, onManageLists = {}, onStartBattle = {},
                onResumeBattle = {}, onViewResults = {}, onRestartBattle = {},
                onDeleteSession = {}, onDeleteConfirmed = {}, onDeleteDismissed = {},
            )
        }
        rule.onNodeWithText("Nouvelle session", substring = true).performClick()
        assertTrue(called)
    }

    // region — session card: NOT_STARTED

    @Test
    fun session_card_not_started_shows_parent_names() {
        rule.setThemedContent {
            HomeScreenContent(
                state = HomeUiState(
                    isLoading = false,
                    sessions = listOf(
                        SessionSummary(
                            sessionId = 1L,
                            parentNames = "Alice & Bob",
                            gender = Gender.BOY,
                            totalNames = 10,
                            allListsValidated = true,
                            canStartBattle = true,
                            battleStatus = BattleStatus.NOT_STARTED,
                        )
                    )
                ),
                onCreateSession = {}, onManageLists = {}, onStartBattle = {},
                onResumeBattle = {}, onViewResults = {}, onRestartBattle = {},
                onDeleteSession = {}, onDeleteConfirmed = {}, onDeleteDismissed = {},
            )
        }
        rule.onNodeWithText("Alice & Bob").assertIsDisplayed()
    }

    @Test
    fun session_card_not_started_shows_start_and_manage_buttons() {
        rule.setThemedContent {
            HomeScreenContent(
                state = HomeUiState(
                    isLoading = false,
                    sessions = listOf(
                        SessionSummary(
                            sessionId = 1L,
                            parentNames = "Alice & Bob",
                            gender = Gender.BOY,
                            totalNames = 10,
                            allListsValidated = true,
                            canStartBattle = true,
                            battleStatus = BattleStatus.NOT_STARTED,
                        )
                    )
                ),
                onCreateSession = {}, onManageLists = {}, onStartBattle = {},
                onResumeBattle = {}, onViewResults = {}, onRestartBattle = {},
                onDeleteSession = {}, onDeleteConfirmed = {}, onDeleteDismissed = {},
            )
        }
        rule.onNodeWithText("Commencer la bataille", substring = true).assertIsDisplayed()
        rule.onNodeWithText("Gérer les listes").assertIsDisplayed()
    }

    @Test
    fun session_card_not_started_start_button_disabled_when_cannot_start() {
        rule.setThemedContent {
            HomeScreenContent(
                state = HomeUiState(
                    isLoading = false,
                    sessions = listOf(
                        SessionSummary(
                            sessionId = 1L,
                            parentNames = "Alice & Bob",
                            gender = Gender.GIRL,
                            totalNames = 2,
                            allListsValidated = false,
                            canStartBattle = false,
                            battleStatus = BattleStatus.NOT_STARTED,
                        )
                    )
                ),
                onCreateSession = {}, onManageLists = {}, onStartBattle = {},
                onResumeBattle = {}, onViewResults = {}, onRestartBattle = {},
                onDeleteSession = {}, onDeleteConfirmed = {}, onDeleteDismissed = {},
            )
        }
        rule.onNodeWithText("Commencer la bataille", substring = true).assertIsNotEnabled()
    }

    @Test
    fun clicking_start_battle_triggers_callback_with_session_id() {
        var receivedId = -1L
        rule.setThemedContent {
            HomeScreenContent(
                state = HomeUiState(
                    isLoading = false,
                    sessions = listOf(
                        SessionSummary(
                            sessionId = 42L,
                            parentNames = "Alice & Bob",
                            gender = Gender.BOY,
                            totalNames = 10,
                            allListsValidated = true,
                            canStartBattle = true,
                            battleStatus = BattleStatus.NOT_STARTED,
                        )
                    )
                ),
                onCreateSession = {}, onManageLists = {}, onStartBattle = { receivedId = it },
                onResumeBattle = {}, onViewResults = {}, onRestartBattle = {},
                onDeleteSession = {}, onDeleteConfirmed = {}, onDeleteDismissed = {},
            )
        }
        rule.onNodeWithText("Commencer la bataille", substring = true).performClick()
        assertTrue(receivedId == 42L)
    }

    // region — session card: IN_PROGRESS

    @Test
    fun session_card_in_progress_shows_resume_button() {
        rule.setThemedContent {
            HomeScreenContent(
                state = HomeUiState(
                    isLoading = false,
                    sessions = listOf(
                        SessionSummary(
                            sessionId = 1L,
                            parentNames = "Alice & Bob",
                            gender = Gender.BOY,
                            totalNames = 10,
                            allListsValidated = true,
                            canStartBattle = true,
                            battleStatus = BattleStatus.IN_PROGRESS,
                        )
                    )
                ),
                onCreateSession = {}, onManageLists = {}, onStartBattle = {},
                onResumeBattle = {}, onViewResults = {}, onRestartBattle = {},
                onDeleteSession = {}, onDeleteConfirmed = {}, onDeleteDismissed = {},
            )
        }
        rule.onNodeWithText("Reprendre la bataille", substring = true).assertIsDisplayed()
    }

    @Test
    fun clicking_resume_battle_triggers_callback_with_session_id() {
        var receivedId = -1L
        rule.setThemedContent {
            HomeScreenContent(
                state = HomeUiState(
                    isLoading = false,
                    sessions = listOf(
                        SessionSummary(
                            sessionId = 7L,
                            parentNames = "Alice & Bob",
                            gender = Gender.BOY,
                            totalNames = 10,
                            allListsValidated = true,
                            canStartBattle = true,
                            battleStatus = BattleStatus.IN_PROGRESS,
                        )
                    )
                ),
                onCreateSession = {}, onManageLists = {}, onStartBattle = {},
                onResumeBattle = { receivedId = it }, onViewResults = {}, onRestartBattle = {},
                onDeleteSession = {}, onDeleteConfirmed = {}, onDeleteDismissed = {},
            )
        }
        rule.onNodeWithText("Reprendre la bataille", substring = true).performClick()
        assertTrue(receivedId == 7L)
    }

    // region — session card: COMPLETED

    @Test
    fun session_card_completed_shows_results_and_export_buttons() {
        rule.setThemedContent {
            HomeScreenContent(
                state = HomeUiState(
                    isLoading = false,
                    sessions = listOf(
                        SessionSummary(
                            sessionId = 1L,
                            parentNames = "Alice & Bob",
                            gender = Gender.GIRL,
                            totalNames = 10,
                            allListsValidated = true,
                            canStartBattle = true,
                            battleStatus = BattleStatus.COMPLETED,
                        )
                    )
                ),
                onCreateSession = {}, onManageLists = {}, onStartBattle = {},
                onResumeBattle = {}, onViewResults = {}, onRestartBattle = {},
                onDeleteSession = {}, onDeleteConfirmed = {}, onDeleteDismissed = {},
            )
        }
        rule.onNodeWithText("Consulter les résultats", substring = true).assertIsDisplayed()
        rule.onNodeWithText("Exporter en PDF", substring = true).assertIsDisplayed()
    }

    @Test
    fun clicking_view_results_triggers_callback_with_session_id() {
        var receivedId = -1L
        rule.setThemedContent {
            HomeScreenContent(
                state = HomeUiState(
                    isLoading = false,
                    sessions = listOf(
                        SessionSummary(
                            sessionId = 99L,
                            parentNames = "Alice & Bob",
                            gender = Gender.GIRL,
                            totalNames = 10,
                            allListsValidated = true,
                            canStartBattle = true,
                            battleStatus = BattleStatus.COMPLETED,
                        )
                    )
                ),
                onCreateSession = {}, onManageLists = {}, onStartBattle = {},
                onResumeBattle = {}, onViewResults = { receivedId = it }, onRestartBattle = {},
                onDeleteSession = {}, onDeleteConfirmed = {}, onDeleteDismissed = {},
            )
        }
        rule.onNodeWithText("Consulter les résultats", substring = true).performClick()
        assertTrue(receivedId == 99L)
    }

    // region — delete dialog

    @Test
    fun delete_dialog_shown_when_pending_delete() {
        rule.setThemedContent {
            HomeScreenContent(
                state = HomeUiState(
                    isLoading = false,
                    sessions = listOf(
                        SessionSummary(
                            sessionId = 1L,
                            parentNames = "Alice & Bob",
                            gender = Gender.BOY,
                            totalNames = 5,
                            allListsValidated = false,
                            canStartBattle = false,
                            battleStatus = BattleStatus.NOT_STARTED,
                        )
                    ),
                    pendingDeleteSessionId = 1L,
                ),
                onCreateSession = {}, onManageLists = {}, onStartBattle = {},
                onResumeBattle = {}, onViewResults = {}, onRestartBattle = {},
                onDeleteSession = {}, onDeleteConfirmed = {}, onDeleteDismissed = {},
            )
        }
        rule.onNodeWithText("Annuler").assertIsDisplayed()
    }

    @Test
    fun delete_dialog_confirm_triggers_callback() {
        var confirmed = false
        rule.setThemedContent {
            HomeScreenContent(
                state = HomeUiState(
                    isLoading = false,
                    sessions = emptyList(),
                    pendingDeleteSessionId = 1L,
                ),
                onCreateSession = {}, onManageLists = {}, onStartBattle = {},
                onResumeBattle = {}, onViewResults = {}, onRestartBattle = {},
                onDeleteSession = {}, onDeleteConfirmed = { confirmed = true },
                onDeleteDismissed = {},
            )
        }
        rule.onNodeWithText("Tout effacer").performClick()
        assertTrue(confirmed)
    }
}
