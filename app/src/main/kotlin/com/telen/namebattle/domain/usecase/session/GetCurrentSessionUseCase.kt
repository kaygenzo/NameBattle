package com.telen.namebattle.domain.usecase.session

import com.telen.namebattle.domain.model.Session
import com.telen.namebattle.domain.repository.SessionRepository

class GetSessionUseCase(private val repo: SessionRepository) {
    suspend operator fun invoke(sessionId: Long): Session? = repo.getSessionById(sessionId)
}
