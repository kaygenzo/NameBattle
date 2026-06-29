package com.telen.namebattle.presentation.battle

enum class BattleMode { DUEL, AUTO_PASS, ROUND_SUMMARY }

data class RoundSummary(
    val finishedRound: Int,
    val nextRound: Int,
    val survivors: List<String>,
    val eliminated: List<String>,
    val nextDuels: Int,
    val nextHasAuto: Boolean,
    val target: Int,
)

data class BattleUiState(
    val isLoading: Boolean = true,
    val mode: BattleMode = BattleMode.DUEL,
    val roundNumber: Int = 1,
    val position: String = "0/0",
    val progress: Float = 0f,
    val duelKey: Int = 0,
    val leftId: Long = 0,
    val leftName: String = "",
    val rightId: Long = 0,
    val rightName: String = "",
    val autoId: Long = 0,
    val autoName: String = "",
    val summary: RoundSummary? = null,
)
