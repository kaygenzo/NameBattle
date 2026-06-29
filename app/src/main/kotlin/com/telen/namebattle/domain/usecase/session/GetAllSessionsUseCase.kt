package com.telen.namebattle.domain.usecase.session

import com.telen.namebattle.domain.model.Session
import com.telen.namebattle.domain.repository.SessionRepository

class GetAllSessionsUseCase(private val repo: SessionRepository) {
    suspend operator fun invoke(): List<Session> = repo.getAllSessions()
}
