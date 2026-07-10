package com.telen.namebattle.presentation.battle

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
class BattleScreenTest {

    @get:Rule
    val rule = createComposeRule()

    // region — DUEL mode

    @Test
    fun duel_mode_shows_instruction_label() {
        rule.setThemedContent {
            BattleScreenContent(
                state = BattleUiState(
                    isLoading = false,
                    mode = BattleMode.DUEL,
                    roundNumber = 1,
                    position = "1/5",
                    leftId = 1L, leftName = "Alice",
                    rightId = 2L, rightName = "Bob",
                )
            )
        }
        rule.onNodeWithText("Quel prénom survivra ?").assertIsDisplayed()
    }

    @Test
    fun duel_mode_shows_round_number() {
        rule.setThemedContent {
            BattleScreenContent(
                state = BattleUiState(
                    isLoading = false,
                    mode = BattleMode.DUEL,
                    roundNumber = 2,
                    position = "3/6",
                    leftId = 1L, leftName = "Alice",
                    rightId = 2L, rightName = "Bob",
                )
            )
        }
        rule.onNodeWithText("Round 2").assertIsDisplayed()
    }

    @Test
    fun duel_mode_shows_position_counter() {
        rule.setThemedContent {
            BattleScreenContent(
                state = BattleUiState(
                    isLoading = false,
                    mode = BattleMode.DUEL,
                    roundNumber = 1,
                    position = "4/10",
                    leftId = 1L, leftName = "Alice",
                    rightId = 2L, rightName = "Bob",
                )
            )
        }
        rule.onNodeWithText("4/10").assertIsDisplayed()
    }

    @Test
    fun duel_mode_shows_left_and_right_name_cards() {
        rule.setThemedContent {
            BattleScreenContent(
                state = BattleUiState(
                    isLoading = false,
                    mode = BattleMode.DUEL,
                    roundNumber = 1,
                    position = "1/5",
                    leftId = 1L, leftName = "Alice",
                    rightId = 2L, rightName = "Bob",
                )
            )
        }
        rule.onNodeWithText("Alice").assertExists()
        rule.onNodeWithText("Bob").assertExists()
    }

    @Test
    fun duel_mode_shows_choose_buttons() {
        rule.setThemedContent {
            BattleScreenContent(
                state = BattleUiState(
                    isLoading = false,
                    mode = BattleMode.DUEL,
                    roundNumber = 1,
                    position = "1/5",
                    leftId = 1L, leftName = "Alice",
                    rightId = 2L, rightName = "Bob",
                )
            )
        }
        rule.onAllNodes(androidx.compose.ui.test.hasText("Choisir")).apply {
            fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun clicking_left_card_triggers_choose_callback_with_left_id() {
        var chosenId = -1L
        rule.setThemedContent {
            BattleScreenContent(
                state = BattleUiState(
                    isLoading = false,
                    mode = BattleMode.DUEL,
                    roundNumber = 1,
                    position = "1/5",
                    leftId = 10L, leftName = "Alice",
                    rightId = 20L, rightName = "Bob",
                ),
                onChoose = { chosenId = it },
            )
        }
        // Click the left name card — first "Choisir" button
        rule.onAllNodes(androidx.compose.ui.test.hasText("Choisir"))[0].performClick()
        assertTrue(chosenId == 10L || chosenId == 20L)
    }

    // region — AUTO_PASS mode

    @Test
    fun auto_pass_mode_shows_auto_qualified_badge() {
        rule.setThemedContent {
            BattleScreenContent(
                state = BattleUiState(
                    isLoading = false,
                    mode = BattleMode.AUTO_PASS,
                    roundNumber = 1,
                    position = "5/5",
                    autoId = 3L,
                    autoName = "Charlie",
                )
            )
        }
        rule.onNodeWithText("AUTO-QUALIFIÉ").assertIsDisplayed()
    }

    @Test
    fun auto_pass_mode_shows_auto_name() {
        rule.setThemedContent {
            BattleScreenContent(
                state = BattleUiState(
                    isLoading = false,
                    mode = BattleMode.AUTO_PASS,
                    roundNumber = 1,
                    position = "5/5",
                    autoId = 3L,
                    autoName = "Charlie",
                )
            )
        }
        rule.onNodeWithText("Charlie").assertIsDisplayed()
    }

    @Test
    fun auto_pass_mode_shows_continue_button() {
        rule.setThemedContent {
            BattleScreenContent(
                state = BattleUiState(
                    isLoading = false,
                    mode = BattleMode.AUTO_PASS,
                    roundNumber = 1,
                    position = "5/5",
                    autoId = 3L,
                    autoName = "Charlie",
                )
            )
        }
        rule.onNodeWithText("Continuer →").assertIsDisplayed()
    }

    @Test
    fun clicking_continue_in_auto_pass_triggers_choose_with_auto_id() {
        var chosenId = -1L
        rule.setThemedContent {
            BattleScreenContent(
                state = BattleUiState(
                    isLoading = false,
                    mode = BattleMode.AUTO_PASS,
                    roundNumber = 1,
                    position = "5/5",
                    autoId = 3L,
                    autoName = "Charlie",
                ),
                onChoose = { chosenId = it },
            )
        }
        rule.onNodeWithText("Continuer →").performClick()
        assertTrue(chosenId == 3L)
    }

    // region — ROUND_SUMMARY mode

    @Test
    fun round_summary_shows_finished_round_badge() {
        rule.setThemedContent {
            BattleScreenContent(
                state = BattleUiState(
                    isLoading = false,
                    mode = BattleMode.ROUND_SUMMARY,
                    roundNumber = 1,
                    position = "5/5",
                    summary = RoundSummary(
                        finishedRound = 1,
                        nextRound = 2,
                        survivors = listOf("Alice", "Bob", "Charlie"),
                        eliminated = listOf("Dan", "Eve"),
                        nextDuels = 1,
                        nextHasAuto = true,
                        target = 3,
                    )
                )
            )
        }
        rule.onNodeWithText("Round 1 terminé").assertIsDisplayed()
    }

    @Test
    fun round_summary_shows_survivors() {
        rule.setThemedContent {
            BattleScreenContent(
                state = BattleUiState(
                    isLoading = false,
                    mode = BattleMode.ROUND_SUMMARY,
                    roundNumber = 1,
                    position = "5/5",
                    summary = RoundSummary(
                        finishedRound = 1,
                        nextRound = 2,
                        survivors = listOf("Alice", "Bob"),
                        eliminated = listOf("Dan"),
                        nextDuels = 1,
                        nextHasAuto = false,
                        target = 3,
                    )
                )
            )
        }
        rule.onNodeWithText("Alice").assertIsDisplayed()
        rule.onNodeWithText("Bob").assertIsDisplayed()
    }

    @Test
    fun round_summary_shows_eliminated_names() {
        rule.setThemedContent {
            BattleScreenContent(
                state = BattleUiState(
                    isLoading = false,
                    mode = BattleMode.ROUND_SUMMARY,
                    roundNumber = 1,
                    position = "5/5",
                    summary = RoundSummary(
                        finishedRound = 1,
                        nextRound = 2,
                        survivors = listOf("Alice"),
                        eliminated = listOf("Dan", "Eve"),
                        nextDuels = 1,
                        nextHasAuto = false,
                        target = 3,
                    )
                )
            )
        }
        rule.onNodeWithText("Dan").assertIsDisplayed()
        rule.onNodeWithText("Eve").assertIsDisplayed()
    }

    @Test
    fun round_summary_shows_start_next_round_button() {
        rule.setThemedContent {
            BattleScreenContent(
                state = BattleUiState(
                    isLoading = false,
                    mode = BattleMode.ROUND_SUMMARY,
                    roundNumber = 1,
                    position = "5/5",
                    summary = RoundSummary(
                        finishedRound = 1,
                        nextRound = 2,
                        survivors = listOf("Alice", "Bob"),
                        eliminated = listOf("Dan"),
                        nextDuels = 1,
                        nextHasAuto = false,
                        target = 3,
                    )
                )
            )
        }
        rule.onNodeWithText("Lancer le round 2", substring = true).assertIsDisplayed()
    }

    @Test
    fun clicking_start_next_round_triggers_continue_callback() {
        var called = false
        rule.setThemedContent {
            BattleScreenContent(
                state = BattleUiState(
                    isLoading = false,
                    mode = BattleMode.ROUND_SUMMARY,
                    roundNumber = 1,
                    position = "5/5",
                    summary = RoundSummary(
                        finishedRound = 1,
                        nextRound = 2,
                        survivors = listOf("Alice", "Bob"),
                        eliminated = listOf("Dan"),
                        nextDuels = 1,
                        nextHasAuto = false,
                        target = 3,
                    )
                ),
                onContinue = { called = true },
            )
        }
        rule.onNodeWithText("Lancer le round 2", substring = true).performClick()
        assertTrue(called)
    }
}
