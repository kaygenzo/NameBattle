package com.telen.namebattle.presentation.battle

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.telen.namebattle.presentation.theme.NameBattleTheme

@Preview(
    name = "Battle – Duel – Dark",
    showBackground = true,
    backgroundColor = 0xFF0F0F11,
    showSystemUi = true,
)
@Composable
private fun BattleDuelDark() {
    NameBattleTheme {
        BattleScreenContent(
            state = BattleUiState(
                isLoading = false,
                mode = BattleMode.DUEL,
                roundNumber = 1,
                position = "2/5",
                progress = 0.4f,
                duelKey = 1,
                leftId = 1L, leftName = "Élodie",
                rightId = 2L, rightName = "Camille",
            )
        )
    }
}

@Preview(
    name = "Battle – Duel – Light",
    showBackground = true,
    backgroundColor = 0xFFF5F5FA,
    showSystemUi = true,
)
@Composable
private fun BattleDuelLight() {
    NameBattleTheme {
        BattleScreenContent(
            state = BattleUiState(
                isLoading = false,
                mode = BattleMode.DUEL,
                roundNumber = 2,
                position = "1/4",
                progress = 0.25f,
                duelKey = 3,
                leftId = 1L, leftName = "Élodie",
                rightId = 2L, rightName = "Camille",
            )
        )
    }
}

@Preview(
    name = "Battle – Auto-qualifié – Dark",
    showBackground = true,
    backgroundColor = 0xFF0F0F11,
    showSystemUi = true,
)
@Composable
private fun BattleAutoPassDark() {
    NameBattleTheme {
        BattleScreenContent(
            state = BattleUiState(
                isLoading = false,
                mode = BattleMode.AUTO_PASS,
                roundNumber = 2,
                position = "3/3",
                progress = 1f,
                autoId = 5L,
                autoName = "Lucie",
            )
        )
    }
}

@Preview(
    name = "Battle – Résumé de round – Dark",
    showBackground = true,
    backgroundColor = 0xFF0F0F11,
    showSystemUi = true,
)
@Composable
private fun BattleRoundSummaryDark() {
    NameBattleTheme {
        BattleScreenContent(
            state = BattleUiState(
                isLoading = false,
                mode = BattleMode.ROUND_SUMMARY,
                roundNumber = 2,
                position = "5/5",
                progress = 1f,
                summary = RoundSummary(
                    finishedRound = 1,
                    nextRound = 2,
                    survivors = listOf("Élodie", "Camille", "Lucie"),
                    eliminated = listOf("Sophie", "Marie"),
                    nextDuels = 1,
                    nextHasAuto = true,
                    target = 3,
                )
            )
        )
    }
}
