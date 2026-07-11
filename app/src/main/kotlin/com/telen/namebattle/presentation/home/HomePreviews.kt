package com.telen.namebattle.presentation.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.telen.namebattle.domain.model.Gender
import com.telen.namebattle.presentation.theme.NameBattleTheme

private val previewSessions = listOf(
    SessionSummary(
        sessionId = 1L,
        parentNames = "Sofia & Thomas",
        gender = Gender.BOY,
        totalNames = 12,
        allListsValidated = true,
        canStartBattle = true,
        battleStatus = BattleStatus.NOT_STARTED,
    ),
    SessionSummary(
        sessionId = 2L,
        parentNames = "Marie",
        gender = Gender.GIRL,
        totalNames = 7,
        allListsValidated = true,
        canStartBattle = true,
        battleStatus = BattleStatus.IN_PROGRESS,
    ),
    SessionSummary(
        sessionId = 3L,
        parentNames = "Lucie & Marc",
        gender = Gender.GIRL,
        totalNames = 9,
        allListsValidated = true,
        canStartBattle = true,
        battleStatus = BattleStatus.COMPLETED,
    ),
)

private fun homeContent(state: HomeUiState) = @Composable {
    HomeScreenContent(
        state = state,
        onCreateSession = {},
        onManageLists = {},
        onStartBattle = {},
        onResumeBattle = {},
        onViewResults = {},
        onRestartBattle = {},
        onAbout = {},
        onDeleteSession = {},
        onDeleteConfirmed = {},
        onDeleteDismissed = {},
    )
}

@Preview(
    name = "Home – Sessions (not started / in progress / completed) – Dark",
    showBackground = true,
    backgroundColor = 0xFF0F0F11,
    showSystemUi = true,
)
@Composable
private fun HomeWithSessionsDark() {
    NameBattleTheme { homeContent(HomeUiState(isLoading = false, sessions = previewSessions))() }
}

@Preview(
    name = "Home – Sessions – Light",
    showBackground = true,
    backgroundColor = 0xFFF5F5FA,
    showSystemUi = true,
)
@Composable
private fun HomeWithSessionsLight() {
    NameBattleTheme { homeContent(HomeUiState(isLoading = false, sessions = previewSessions))() }
}

@Preview(
    name = "Home – Vide – Dark",
    showBackground = true,
    backgroundColor = 0xFF0F0F11,
    showSystemUi = true,
)
@Composable
private fun HomeEmptyDark() {
    NameBattleTheme { homeContent(HomeUiState(isLoading = false, sessions = emptyList()))() }
}

@Preview(
    name = "Home – Loading – Dark",
    showBackground = true,
    backgroundColor = 0xFF0F0F11,
    showSystemUi = true,
)
@Composable
private fun HomeLoadingDark() {
    NameBattleTheme { homeContent(HomeUiState(isLoading = true))() }
}
