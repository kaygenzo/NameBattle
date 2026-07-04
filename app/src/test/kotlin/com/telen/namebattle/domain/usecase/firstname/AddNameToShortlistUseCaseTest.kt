package com.telen.namebattle.domain.usecase.firstname

import com.telen.namebattle.domain.repository.SessionRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class AddNameToShortlistUseCaseTest {

    private lateinit var sessionRepository: SessionRepository
    private lateinit var useCase: AddNameToShortlistUseCase

    @Before
    fun setUp() {
        sessionRepository = mockk()
        useCase = AddNameToShortlistUseCase(sessionRepository)
    }

    @Test
    fun `delegates to repo addNameToShortlist with the given parentId and firstNameId`() = runTest {
        // given
        coEvery { sessionRepository.addNameToShortlist(parentId = 1L, firstNameId = 10L) } returns Unit

        // when
        useCase(parentId = 1L, firstNameId = 10L)

        // then
        coVerify(exactly = 1) { sessionRepository.addNameToShortlist(parentId = 1L, firstNameId = 10L) }
    }
}
