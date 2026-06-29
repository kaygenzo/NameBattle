package com.telen.namebattle.presentation.home

import com.telen.namebattle.domain.model.Gender

enum class BattleStatus { NOT_STARTED, IN_PROGRESS, COMPLETED }

data class SessionSummary(
    val sessionId: Long,
    val parentNames: String,
    val genderLabel: String,
    val gender: Gender,
    val totalNames: Int,
    val allListsValidated: Boolean,
    val canStartBattle: Boolean,
    val battleStatus: BattleStatus = BattleStatus.NOT_STARTED,
)

data class HomeUiState(
    val isLoading: Boolean = true,
    val sessions: List<SessionSummary> = emptyList(),
    val pendingDeleteSessionId: Long? = null,
)
