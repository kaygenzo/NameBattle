package com.telen.namebattle.domain.usecase.battle

import com.telen.namebattle.domain.model.BattleState
import com.telen.namebattle.domain.repository.BattleRepository

class ChooseWinnerUseCase(private val battleRepo: BattleRepository) {
    suspend operator fun invoke(sessionId: Long, winnerId: Long): BattleState {
        val current = battleRepo.getBattleState(sessionId)
            ?: error("No battle state for session $sessionId")
        val updated = current.chooseWinner(winnerId)
        battleRepo.saveBattleState(updated)
        return updated
    }
}
