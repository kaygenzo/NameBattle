package com.telen.namebattle.domain.usecase.session

import com.telen.namebattle.domain.repository.SessionRepository
import com.telen.namebattle.util.buildSession
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GetAllSessionsUseCaseTest {

    private lateinit var sessionRepository: SessionRepository
    private lateinit var useCase: GetAllSessionsUseCase

    @Before
    fun setUp() {
        sessionRepository = mockk()
        useCase = GetAllSessionsUseCase(sessionRepository)
    }

    @Test
    fun `returns the list of sessions from repository`() = runTest {
        // given
        val sessions = listOf(buildSession(id = 1L), buildSession(id = 2L))
        coEvery { sessionRepository.getAllSessions() } returns sessions

        // when
        val result = useCase()

        // then
        assertEquals(sessions, result)
        coVerify(exactly = 1) { sessionRepository.getAllSessions() }
    }

    @Test
    fun `returns empty list when repository has no sessions`() = runTest {
        // given
        coEvery { sessionRepository.getAllSessions() } returns emptyList()

        // when
        val result = useCase()

        // then
        assertEquals(emptyList<com.telen.namebattle.domain.model.Session>(), result)
    }
}
