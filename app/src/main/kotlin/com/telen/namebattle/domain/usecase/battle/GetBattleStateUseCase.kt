package com.telen.namebattle.domain.usecase.battle

import com.telen.namebattle.domain.model.BattleState
import com.telen.namebattle.domain.repository.BattleRepository

class GetBattleStateUseCase(private val battleRepo: BattleRepository) {
    suspend operator fun invoke(sessionId: Long): BattleState? =
        battleRepo.getBattleState(sessionId)
}
