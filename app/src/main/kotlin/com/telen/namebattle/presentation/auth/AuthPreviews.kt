package com.telen.namebattle.presentation.auth

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.telen.namebattle.presentation.theme.NameBattleTheme

private val twoParents = listOf(
    ParentOption(parentIndex = 0, name = "Sofia", shortlistCount = 8, listValidated = true),
    ParentOption(parentIndex = 1, name = "Thomas", shortlistCount = 4, listValidated = false),
)

@Preview(name = "Auth – Sélection – Dark", showBackground = true, backgroundColor = 0xFF0F0F11, showSystemUi = true)
@Composable
private fun AuthSelectionDark() {
    NameBattleTheme {
        AuthScreenContent(
            state = AuthUiState(isLoading = false, parents = twoParents, totalNames = 12)
        )
    }
}

@Preview(name = "Auth – Sélection – Light", showBackground = true, backgroundColor = 0xFFF5F5FA, showSystemUi = true)
@Composable
private fun AuthSelectionLight() {
    NameBattleTheme {
        AuthScreenContent(
            state = AuthUiState(isLoading = false, parents = twoParents, totalNames = 12)
        )
    }
}

@Preview(name = "Auth – Parent sélectionné – Dark", showBackground = true, backgroundColor = 0xFF0F0F11, showSystemUi = true)
@Composable
private fun AuthParentSelectedDark() {
    NameBattleTheme {
        AuthScreenContent(
            state = AuthUiState(
                isLoading = false,
                parents = twoParents,
                totalNames = 12,
                selectedParentIndex = 0,
                password = "secret",
            )
        )
    }
}

@Preview(name = "Auth – Erreur mot de passe – Dark", showBackground = true, backgroundColor = 0xFF0F0F11, showSystemUi = true)
@Composable
private fun AuthErrorDark() {
    NameBattleTheme {
        AuthScreenContent(
            state = AuthUiState(
                isLoading = false,
                parents = twoParents,
                totalNames = 12,
                selectedParentIndex = 0,
                password = "mauvais",
                error = "Mot de passe incorrect",
            )
        )
    }
}

@Preview(name = "Auth – Bouton lancer bataille – Dark", showBackground = true, backgroundColor = 0xFF0F0F11, showSystemUi = true)
@Composable
private fun AuthCanLaunchDark() {
    NameBattleTheme {
        AuthScreenContent(
            state = AuthUiState(
                isLoading = false,
                parents = listOf(
                    ParentOption(0, "Sofia", 8, listValidated = true),
                    ParentOption(1, "Thomas", 5, listValidated = true),
                ),
                totalNames = 13,
            )
        )
    }
}

@Preview(name = "Auth – Solo parent – Dark", showBackground = true, backgroundColor = 0xFF0F0F11, showSystemUi = true)
@Composable
private fun AuthSoloDark() {
    NameBattleTheme {
        AuthScreenContent(
            state = AuthUiState(
                isLoading = false,
                parents = listOf(ParentOption(0, "Sofia", 6, listValidated = true)),
                totalNames = 6,
            )
        )
    }
}
