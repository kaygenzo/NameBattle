package com.telen.namebattle.presentation.setup

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.telen.namebattle.domain.model.Gender
import com.telen.namebattle.presentation.theme.NameBattleTheme

@Preview(
    name = "Setup – Étape initiale – Dark",
    showBackground = true,
    backgroundColor = 0xFF0F0F11,
    showSystemUi = true,
)
@Composable
private fun SetupInitialDark() {
    NameBattleTheme {
        SetupScreenContent(state = SetupUiState())
    }
}

@Preview(
    name = "Setup – Étape initiale – Light",
    showBackground = true,
    backgroundColor = 0xFFF5F5FA,
    showSystemUi = true,
)
@Composable
private fun SetupInitialLight() {
    NameBattleTheme {
        SetupScreenContent(state = SetupUiState())
    }
}

@Preview(
    name = "Setup – Parent 1 verrouillé – Dark",
    showBackground = true,
    backgroundColor = 0xFF0F0F11,
    showSystemUi = true,
)
@Composable
private fun SetupParent1LockedDark() {
    NameBattleTheme {
        SetupScreenContent(
            state = SetupUiState(
                gender = Gender.BOY,
                parent1Name = "Sofia",
                parent1Password = "secret",
                parent1Confirm = "secret",
                parent1Locked = true,
            )
        )
    }
}

@Preview(
    name = "Setup – Erreur confirmation – Dark",
    showBackground = true,
    backgroundColor = 0xFF0F0F11,
    showSystemUi = true,
)
@Composable
private fun SetupPasswordMismatchDark() {
    NameBattleTheme {
        SetupScreenContent(
            state = SetupUiState(
                parent1Name = "Sofia",
                parent1Password = "secret",
                parent1Confirm = "autre",
            )
        )
    }
}

@Preview(
    name = "Setup – Mode solo – Dark",
    showBackground = true,
    backgroundColor = 0xFF0F0F11,
    showSystemUi = true,
)
@Composable
private fun SetupSoloDark() {
    NameBattleTheme {
        SetupScreenContent(
            state = SetupUiState(
                soloMode = true,
                parent1Name = "Sofia",
                parent1Locked = true,
            )
        )
    }
}

@Preview(
    name = "Setup – Deux parents verrouillés – Dark",
    showBackground = true,
    backgroundColor = 0xFF0F0F11,
    showSystemUi = true,
)
@Composable
private fun SetupBothLockedDark() {
    NameBattleTheme {
        SetupScreenContent(
            state = SetupUiState(
                parent1Name = "Sofia",
                parent1Locked = true,
                parent2Name = "Thomas",
                parent2Locked = true,
            )
        )
    }
}
