package com.telen.namebattle.domain.usecase.firstname

import com.telen.namebattle.domain.repository.SessionRepository

class ValidateParentListUseCase(private val repo: SessionRepository) {
    suspend operator fun invoke(parentId: Long) = repo.validateParentList(parentId)
}
