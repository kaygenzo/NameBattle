package com.telen.namebattle.data.repository

import com.telen.namebattle.data.local.dao.SessionDao
import com.telen.namebattle.domain.model.BattleState
import com.telen.namebattle.domain.repository.BattleRepository
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class BattleRepositoryImpl(private val dao: SessionDao) : BattleRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun getBattleState(sessionId: Long): BattleState? {
        val entity = dao.getSessionById(sessionId) ?: return null
        return entity.battleStateJson?.let { json.decodeFromString(it) }
    }

    override suspend fun saveBattleState(state: BattleState) {
        dao.updateBattleState(state.sessionId, json.encodeToString(state))
    }

    override suspend fun clearBattleState(sessionId: Long) {
        dao.updateBattleState(sessionId, null)
    }
}
