package com.telen.namebattle.domain.usecase.firstname

import com.telen.namebattle.domain.model.FirstName
import com.telen.namebattle.domain.repository.FirstNameRepository

class GetNameDetailUseCase(private val repo: FirstNameRepository) {
    suspend operator fun invoke(id: Long): FirstName? = repo.getNameDetail(id)
}
