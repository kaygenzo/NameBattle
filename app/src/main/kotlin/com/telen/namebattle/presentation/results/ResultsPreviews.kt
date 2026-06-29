package com.telen.namebattle.presentation.results

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.telen.namebattle.presentation.theme.NameBattleTheme

@Preview(name = "Results – 4 finalistes – Dark", showBackground = true, backgroundColor = 0xFF0F0F11, showSystemUi = true)
@Composable
private fun ResultsFourFinalistsDark() {
    NameBattleTheme {
        ResultsScreenContent(
            state = ResultsUiState(
                isLoading = false,
                finalists = listOf("Élodie", "Camille", "Lucie", "Marie"),
                roundsPlayed = 3,
            )
        )
    }
}

@Preview(name = "Results – 4 finalistes – Light", showBackground = true, backgroundColor = 0xFFF5F5FA, showSystemUi = true)
@Composable
private fun ResultsFourFinalistsLight() {
    NameBattleTheme {
        ResultsScreenContent(
            state = ResultsUiState(
                isLoading = false,
                finalists = listOf("Élodie", "Camille", "Lucie", "Marie"),
                roundsPlayed = 3,
            )
        )
    }
}

@Preview(name = "Results – 3 finalistes – Dark", showBackground = true, backgroundColor = 0xFF0F0F11, showSystemUi = true)
@Composable
private fun ResultsThreeFinalistsDark() {
    NameBattleTheme {
        ResultsScreenContent(
            state = ResultsUiState(
                isLoading = false,
                finalists = listOf("Élodie", "Camille", "Lucie"),
                roundsPlayed = 2,
            )
        )
    }
}
