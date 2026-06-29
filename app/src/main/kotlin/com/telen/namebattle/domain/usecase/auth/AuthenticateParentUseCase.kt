package com.telen.namebattle.domain.usecase.auth

import com.telen.namebattle.domain.repository.SessionRepository
import com.telen.namebattle.util.sha256

class AuthenticateParentUseCase(private val repo: SessionRepository) {

    suspend operator fun invoke(sessionId: Long, parentIndex: Int, password: String): Boolean {
        val parent = repo.getParentBySession(sessionId, parentIndex) ?: return false
        val ok = parent.passwordHash == password.sha256()
        if (ok) repo.markParentAuthenticated(parent.id)
        return ok
    }
}
