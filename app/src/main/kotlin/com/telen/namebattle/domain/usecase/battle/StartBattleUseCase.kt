package com.telen.namebattle.domain.usecase.battle

import com.telen.namebattle.domain.model.BattleState
import com.telen.namebattle.domain.model.Session
import com.telen.namebattle.domain.repository.BattleRepository
import com.telen.namebattle.domain.repository.SessionRepository

class StartBattleUseCase(
    private val sessionRepo: SessionRepository,
    private val battleRepo: BattleRepository
) {
    suspend operator fun invoke(session: Session): BattleState {
        val p1ShortlistIds = sessionRepo.getShortlistIds(session.parent1.id).toSet()
        val p2ShortlistIds = session.parent2?.let {
            sessionRepo.getShortlistIds(it.id).toSet()
        } ?: emptySet()
        val allIds = (p1ShortlistIds + p2ShortlistIds).toList()
        val state = BattleState.create(session.id, allIds)
        battleRepo.saveBattleState(state)
        return state
    }
}
