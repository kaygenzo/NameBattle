package com.telen.namebattle.domain.usecase.firstname

import com.telen.namebattle.domain.repository.SessionRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class ValidateParentListUseCaseTest {

    private lateinit var sessionRepository: SessionRepository
    private lateinit var useCase: ValidateParentListUseCase

    @Before
    fun setUp() {
        sessionRepository = mockk()
        useCase = ValidateParentListUseCase(sessionRepository)
    }

    @Test
    fun `delegates to repo validateParentList with the given parentId`() = runTest {
        // given
        coEvery { sessionRepository.validateParentList(parentId = 1L) } returns Unit

        // when
        useCase(parentId = 1L)

        // then
        coVerify(exactly = 1) { sessionRepository.validateParentList(parentId = 1L) }
    }
}
