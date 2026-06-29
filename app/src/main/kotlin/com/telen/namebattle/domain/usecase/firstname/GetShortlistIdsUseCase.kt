package com.telen.namebattle.domain.usecase.firstname

import com.telen.namebattle.domain.repository.SessionRepository

class GetShortlistIdsUseCase(private val repo: SessionRepository) {
    suspend operator fun invoke(parentId: Long): List<Long> = repo.getShortlistIds(parentId)
}
