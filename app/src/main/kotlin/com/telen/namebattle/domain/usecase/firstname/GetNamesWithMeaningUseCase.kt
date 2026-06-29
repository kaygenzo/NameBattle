package com.telen.namebattle.domain.usecase.firstname

import com.telen.namebattle.domain.repository.FirstNameRepository

class GetNamesWithMeaningUseCase(private val repo: FirstNameRepository) {
    suspend operator fun invoke(): Set<String> = repo.namesWithMeaning()
}
