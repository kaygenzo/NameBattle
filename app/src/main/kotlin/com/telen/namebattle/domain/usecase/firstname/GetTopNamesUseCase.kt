package com.telen.namebattle.domain.usecase.firstname

import com.telen.namebattle.domain.model.FirstName
import com.telen.namebattle.domain.model.Gender
import com.telen.namebattle.domain.repository.FirstNameRepository
import kotlinx.coroutines.flow.Flow

class GetTopNamesUseCase(private val repo: FirstNameRepository) {
    operator fun invoke(gender: Gender, fromYear: Int): Flow<List<FirstName>> =
        repo.getTopNames(gender, fromYear)
}
