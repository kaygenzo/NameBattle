package com.telen.namebattle.domain.usecase.battle

import com.telen.namebattle.domain.repository.BattleRepository

class ClearBattleStateUseCase(private val repo: BattleRepository) {
    suspend operator fun invoke(sessionId: Long) = repo.clearBattleState(sessionId)
}
