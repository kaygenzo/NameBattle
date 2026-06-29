package com.telen.namebattle.domain.usecase.firstname

import com.telen.namebattle.domain.model.FirstName
import com.telen.namebattle.domain.model.Gender
import com.telen.namebattle.domain.repository.FirstNameRepository
import kotlinx.coroutines.flow.Flow

class SearchFreeTextUseCase(private val repo: FirstNameRepository) {
    operator fun invoke(query: String, gender: Gender): Flow<List<FirstName>> =
        repo.searchFreeText(query, gender)
}
