package com.telen.namebattle.domain.usecase.firstname

import com.telen.namebattle.domain.repository.SessionRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class RemoveNameFromShortlistUseCaseTest {

    private lateinit var sessionRepository: SessionRepository
    private lateinit var useCase: RemoveNameFromShortlistUseCase

    @Before
    fun setUp() {
        sessionRepository = mockk()
        useCase = RemoveNameFromShortlistUseCase(sessionRepository)
    }

    @Test
    fun `delegates to repo removeNameFromShortlist with the given parentId and firstNameId`()
    = runTest {
        // given
        coEvery {
            sessionRepository.removeNameFromShortlist(parentId = 1L, firstNameId = 10L)
        } returns Unit

        // when
        useCase(parentId = 1L, firstNameId = 10L)

        // then
        coVerify(exactly = 1) {
            sessionRepository.removeNameFromShortlist(parentId = 1L, firstNameId = 10L)
        }
    }
}
