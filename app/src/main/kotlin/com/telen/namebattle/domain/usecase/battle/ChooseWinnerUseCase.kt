package com.telen.namebattle.domain.usecase.battle

import com.telen.namebattle.domain.model.BattleRound
import com.telen.namebattle.domain.model.BattleState
import com.telen.namebattle.domain.model.DuelState
import com.telen.namebattle.domain.repository.BattleRepository

class ChooseWinnerUseCase(private val battleRepo: BattleRepository) {

    suspend operator fun invoke(sessionId: Long, winnerId: Long): BattleState {
        val current = battleRepo.getBattleState(sessionId)
            ?: error("No battle state for session $sessionId")
        val updated = advance(current, winnerId)
        battleRepo.saveBattleState(updated)
        return updated
    }

    internal fun advance(state: BattleState, winnerId: Long): BattleState {
        val round = state.rounds.getOrNull(state.currentRoundIndex) ?: return state
        val updatedDuels = round.duels.toMutableList()
        updatedDuels[state.currentDuelIndex] =
            updatedDuels[state.currentDuelIndex].copy(winnerId = winnerId)
        val updatedRound = round.copy(duels = updatedDuels)
        val updatedRounds = state.rounds.toMutableList()
            .also { it[state.currentRoundIndex] = updatedRound }

        val isLastDuel = state.currentDuelIndex >= round.duels.size - 1
        return if (!isLastDuel) {
            state.copy(rounds = updatedRounds, currentDuelIndex = state.currentDuelIndex + 1)
        } else {
            val winners = updatedRound.duels.mapNotNull { it.winnerId }
            if (winners.size <= 4) {
                state.copy(rounds = updatedRounds, finalists = winners, isComplete = true)
            } else {
                val nextRound = buildRound(state.currentRoundIndex + 2, winners)
                state.copy(
                    rounds = updatedRounds + nextRound,
                    currentRoundIndex = state.currentRoundIndex + 1,
                    currentDuelIndex = 0
                )
            }
        }
    }

    private fun buildRound(number: Int, ids: List<Long>): BattleRound {
        val duels = mutableListOf<DuelState>()
        var i = 0
        while (i < ids.size) {
            duels.add(DuelState(firstName1Id = ids[i], firstName2Id = ids.getOrNull(i + 1)))
            i += 2
        }
        return BattleRound(roundNumber = number, duels = duels)
    }
}
