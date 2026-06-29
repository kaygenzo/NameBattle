package com.telen.namebattle.domain.usecase.firstname

import com.telen.namebattle.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow

class GetShortlistIdsFlowUseCase(private val repo: SessionRepository) {
    operator fun invoke(parentId: Long): Flow<List<Long>> = repo.getShortlistIdsFlow(parentId)
}
