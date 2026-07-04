package com.telen.namebattle.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class DuelState(
    val firstName1Id: Long,
    val firstName2Id: Long?,       // null => auto-pass
    val winnerId: Long? = null
)

@Serializable
data class BattleRound(
    val roundNumber: Int,
    val duels: List<DuelState>
)

@Serializable
data class BattleState(
    val sessionId: Long,
    val initialCount: Int,
    val targetFinalists: Int,
    val rounds: List<BattleRound>,
    val currentRoundIndex: Int = 0,
    val currentDuelIndex: Int = 0,
    val finalists: List<Long> = emptyList(),
    val isComplete: Boolean = false
)
