package com.telen.namebattle.domain.usecase.session

import com.telen.namebattle.domain.repository.SessionRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class DeleteSessionUseCaseTest {

    private lateinit var sessionRepository: SessionRepository
    private lateinit var useCase: DeleteSessionUseCase

    @Before
    fun setUp() {
        sessionRepository = mockk()
        useCase = DeleteSessionUseCase(sessionRepository)
    }

    @Test
    fun `delegates to repo deleteSession with the given sessionId`() = runTest {
        // given
        coEvery { sessionRepository.deleteSession(sessionId = 1L) } returns Unit

        // when
        useCase(sessionId = 1L)

        // then
        coVerify(exactly = 1) { sessionRepository.deleteSession(sessionId = 1L) }
    }
}
