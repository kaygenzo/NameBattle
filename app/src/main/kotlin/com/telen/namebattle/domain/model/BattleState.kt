package com.telen.namebattle.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class DuelState(
    val firstName1Id: Long,
    val firstName2Id: Long?,       // null => auto-pass
    val winnerId: Long? = null
) {
    val isAutoPass: Boolean get() = firstName2Id == null
    val isComplete: Boolean get() = winnerId != null
}

@Serializable
data class BattleRound(
    val roundNumber: Int,
    val duels: List<DuelState>
) {
    val isComplete: Boolean get() = duels.all { it.isComplete }
    val winners: List<Long> get() = duels.mapNotNull { it.winnerId }
}

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
) {
    val currentRound: BattleRound? get() = rounds.getOrNull(currentRoundIndex)
    val currentDuel: DuelState? get() = currentRound?.duels?.getOrNull(currentDuelIndex)
    val currentRoundNumber: Int get() = currentRound?.roundNumber ?: 1
    val totalDuelsInRound: Int get() = currentRound?.duels?.size ?: 0
    val duelPositionLabel: String get() = "${currentDuelIndex + 1}/${totalDuelsInRound}"

    companion object {
        fun create(sessionId: Long, firstNameIds: List<Long>): BattleState {
            require(firstNameIds.size >= 2) { "Need at least 2 prénoms" }
            val shuffled = firstNameIds.shuffled()
            return BattleState(
                sessionId = sessionId,
                initialCount = shuffled.size,
                targetFinalists = minOf(4, shuffled.size),
                rounds = listOf(buildRound(1, shuffled))
            )
        }

        fun buildRound(number: Int, ids: List<Long>): BattleRound {
            val duels = mutableListOf<DuelState>()
            var i = 0
            while (i < ids.size) {
                duels.add(DuelState(firstName1Id = ids[i], firstName2Id = ids.getOrNull(i + 1)))
                i += 2
            }
            return BattleRound(roundNumber = number, duels = duels)
        }
    }

    /** Record a winner for the current duel and advance state. */
    fun chooseWinner(winnerId: Long): BattleState {
        val round = currentRound ?: return this
        val updatedDuels = round.duels.toMutableList()
        updatedDuels[currentDuelIndex] = updatedDuels[currentDuelIndex].copy(winnerId = winnerId)
        val updatedRound = round.copy(duels = updatedDuels)
        val updatedRounds = rounds.toMutableList().also { it[currentRoundIndex] = updatedRound }

        val isLastDuel = currentDuelIndex >= round.duels.size - 1
        return if (!isLastDuel) {
            copy(rounds = updatedRounds, currentDuelIndex = currentDuelIndex + 1)
        } else {
            val winners = updatedRound.winners
            if (winners.size <= 4) {
                copy(rounds = updatedRounds, finalists = winners, isComplete = true)
            } else {
                val nextRound = buildRound(currentRoundIndex + 2, winners)
                copy(
                    rounds = updatedRounds + nextRound,
                    currentRoundIndex = currentRoundIndex + 1,
                    currentDuelIndex = 0
                )
            }
        }
    }
}
