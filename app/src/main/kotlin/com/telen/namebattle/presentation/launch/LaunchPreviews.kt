package com.telen.namebattle.presentation.launch

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.telen.namebattle.presentation.theme.NameBattleTheme

@Preview(
    name = "Launch – Deux parents – Dark",
    showBackground = true,
    backgroundColor = 0xFF0F0F11,
    showSystemUi = true,
)
@Composable
private fun LaunchTwoParentsDark() {
    NameBattleTheme {
        LaunchScreenContent(
            state = LaunchUiState(
                isLoading = false,
                parent1Name = "Sofia",
                parent1Count = 8,
                parent2Name = "Thomas",
                parent2Count = 6,
                total = 14,
                targetFinalists = 4,
                roundsEstimate = 2,
            )
        )
    }
}

@Preview(
    name = "Launch – Deux parents – Light",
    showBackground = true,
    backgroundColor = 0xFFF5F5FA,
    showSystemUi = true,
)
@Composable
private fun LaunchTwoParentsLight() {
    NameBattleTheme {
        LaunchScreenContent(
            state = LaunchUiState(
                isLoading = false,
                parent1Name = "Sofia",
                parent1Count = 8,
                parent2Name = "Thomas",
                parent2Count = 6,
                total = 14,
                targetFinalists = 4,
                roundsEstimate = 2,
            )
        )
    }
}

@Preview(
    name = "Launch – Solo – Dark",
    showBackground = true,
    backgroundColor = 0xFF0F0F11,
    showSystemUi = true,
)
@Composable
private fun LaunchSoloDark() {
    NameBattleTheme {
        LaunchScreenContent(
            state = LaunchUiState(
                isLoading = false,
                parent1Name = "Marie",
                parent1Count = 7,
                parent2Name = null,
                total = 7,
                targetFinalists = 3,
                roundsEstimate = 2,
            )
        )
    }
}

@Preview(
    name = "Launch – Démarrage en cours – Dark",
    showBackground = true,
    backgroundColor = 0xFF0F0F11,
    showSystemUi = true,
)
@Composable
private fun LaunchStartingDark() {
    NameBattleTheme {
        LaunchScreenContent(
            state = LaunchUiState(
                isLoading = false,
                parent1Name = "Sofia",
                parent1Count = 5,
                parent2Name = "Thomas",
                parent2Count = 5,
                total = 10,
                targetFinalists = 4,
                roundsEstimate = 2,
                isStarting = true,
            )
        )
    }
}
