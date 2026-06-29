package com.telen.namebattle.domain.usecase.firstname

import com.telen.namebattle.domain.model.Gender
import com.telen.namebattle.domain.repository.FirstNameRepository
import com.telen.namebattle.domain.repository.SessionRepository

/**
 * Adds a free-text first name (possibly absent from INSEE) to a parent's shortlist.
 * The spelling is materialized as a custom row so the rest of the id-based pipeline
 * (battle, results) needs no special-casing.
 */
class AddCustomNameToShortlistUseCase(
    private val firstNameRepository: FirstNameRepository,
    private val sessionRepository: SessionRepository,
) {
    suspend operator fun invoke(parentId: Long, name: String, gender: Gender) {
        val firstName = firstNameRepository.getOrCreateCustom(name, gender)
        sessionRepository.addNameToShortlist(parentId, firstName.id)
    }
}
