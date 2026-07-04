package com.telen.namebattle.domain.usecase.battle

import com.telen.namebattle.domain.model.BattleRound
import com.telen.namebattle.domain.model.BattleState
import com.telen.namebattle.domain.model.DuelState
import com.telen.namebattle.domain.model.Session
import com.telen.namebattle.domain.repository.BattleRepository
import com.telen.namebattle.domain.repository.SessionRepository

class StartBattleUseCase(
    private val sessionRepo: SessionRepository,
    private val battleRepo: BattleRepository
) {
    suspend operator fun invoke(session: Session): BattleState {
        val p1Ids = sessionRepo.getShortlistIds(session.parent1.id).toSet()
        val p2Ids = session.parent2?.let { sessionRepo.getShortlistIds(it.id).toSet() } ?: emptySet()
        val allIds = (p1Ids + p2Ids).toList()
        require(allIds.size >= 2) { "Need at least 2 prénoms" }
        val shuffled = allIds.shuffled()
        val state = BattleState(
            sessionId = session.id,
            initialCount = shuffled.size,
            targetFinalists = minOf(4, shuffled.size),
            rounds = listOf(buildRound(1, shuffled))
        )
        battleRepo.saveBattleState(state)
        return state
    }

    internal fun buildRound(number: Int, ids: List<Long>): BattleRound {
        val duels = mutableListOf<DuelState>()
        var i = 0
        while (i < ids.size) {
            duels.add(DuelState(firstName1Id = ids[i], firstName2Id = ids.getOrNull(i + 1)))
            i += 2
        }
        return BattleRound(roundNumber = number, duels = duels)
    }
}
