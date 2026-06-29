package com.telen.namebattle.domain.usecase.session

import com.telen.namebattle.domain.model.Gender
import com.telen.namebattle.domain.model.Session
import com.telen.namebattle.domain.repository.SessionRepository
import com.telen.namebattle.util.sha256

class CreateSessionUseCase(private val repo: SessionRepository) {

    data class Params(
        val gender: Gender,
        val parent1Name: String,
        val parent1Password: String,
        val parent2Name: String?,
        val parent2Password: String?
    )

    suspend operator fun invoke(params: Params): Session = repo.createSession(
        gender = params.gender,
        parent1Name = params.parent1Name,
        parent1PasswordHash = params.parent1Password.sha256(),
        parent2Name = params.parent2Name,
        parent2PasswordHash = params.parent2Password?.sha256()
    )
}
