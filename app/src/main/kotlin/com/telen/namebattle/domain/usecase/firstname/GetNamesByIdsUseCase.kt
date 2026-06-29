package com.telen.namebattle.domain.usecase.firstname

import com.telen.namebattle.domain.model.FirstName
import com.telen.namebattle.domain.repository.FirstNameRepository

class GetNamesByIdsUseCase(private val repo: FirstNameRepository) {
    suspend operator fun invoke(ids: List<Long>): List<FirstName> = repo.getByIds(ids)
}
