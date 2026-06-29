package com.telen.namebattle.domain.usecase.firstname

import com.telen.namebattle.domain.repository.SessionRepository

class RemoveNameFromShortlistUseCase(private val repo: SessionRepository) {
    suspend operator fun invoke(parentId: Long, firstNameId: Long) =
        repo.removeNameFromShortlist(parentId, firstNameId)
}
