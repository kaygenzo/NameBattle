package com.telen.namebattle.domain.usecase.session

import com.telen.namebattle.domain.repository.SessionRepository

class DeleteSessionUseCase(private val repo: SessionRepository) {
    suspend operator fun invoke(sessionId: Long) = repo.deleteSession(sessionId)
}
