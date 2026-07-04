package com.telen.namebattle.domain.usecase.session

import com.telen.namebattle.domain.repository.SessionRepository
import com.telen.namebattle.util.buildSession
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class GetSessionUseCaseTest {

    private lateinit var sessionRepository: SessionRepository
    private lateinit var useCase: GetSessionUseCase

    @Before
    fun setUp() {
        sessionRepository = mockk()
        useCase = GetSessionUseCase(sessionRepository)
    }

    @Test
    fun `returns session when found in repository`() = runTest {
        // given
        val expectedSession = buildSession(id = 5L)
        coEvery { sessionRepository.getSessionById(sessionId = 5L) } returns expectedSession

        // when
        val result = useCase(sessionId = 5L)

        // then
        assertEquals(expectedSession, result)
        coVerify(exactly = 1) { sessionRepository.getSessionById(sessionId = 5L) }
    }

    @Test
    fun `returns null when session is not found`() = runTest {
        // given
        coEvery { sessionRepository.getSessionById(sessionId = 99L) } returns null

        // when
        val result = useCase(sessionId = 99L)

        // then
        assertNull(result)
    }
}
