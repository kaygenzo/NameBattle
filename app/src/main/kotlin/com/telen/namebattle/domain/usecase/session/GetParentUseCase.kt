package com.telen.namebattle.domain.usecase.session

import com.telen.namebattle.domain.model.Parent
import com.telen.namebattle.domain.repository.SessionRepository

class GetParentUseCase(private val repo: SessionRepository) {
    suspend operator fun invoke(sessionId: Long, parentIndex: Int): Parent? =
        repo.getParentBySession(sessionId, parentIndex)
}
