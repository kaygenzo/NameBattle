package com.telen.namebattle.domain.repository

import com.telen.namebattle.domain.model.BattleState

interface BattleRepository {
    suspend fun getBattleState(sessionId: Long): BattleState?
    suspend fun saveBattleState(state: BattleState)
    suspend fun clearBattleState(sessionId: Long)
}
