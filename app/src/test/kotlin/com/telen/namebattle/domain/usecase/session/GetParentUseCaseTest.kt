package com.telen.namebattle.domain.usecase.session

import com.telen.namebattle.domain.repository.SessionRepository
import com.telen.namebattle.util.buildParent
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class GetParentUseCaseTest {

    private lateinit var sessionRepository: SessionRepository
    private lateinit var useCase: GetParentUseCase

    @Before
    fun setUp() {
        sessionRepository = mockk()
        useCase = GetParentUseCase(sessionRepository)
    }

    @Test
    fun `delegates to repo getParentBySession and returns the parent`() = runTest {
        // given
        val expectedParent = buildParent(id = 1L, parentIndex = 0)
        coEvery { sessionRepository.getParentBySession(sessionId = 1L, parentIndex = 0) } returns expectedParent

        // when
        val result = useCase(sessionId = 1L, parentIndex = 0)

        // then
        assertEquals(expectedParent, result)
        coVerify(exactly = 1) { sessionRepository.getParentBySession(sessionId = 1L, parentIndex = 0) }
    }

    @Test
    fun `returns null when parent not found`() = runTest {
        // given
        coEvery { sessionRepository.getParentBySession(sessionId = 1L, parentIndex = 1) } returns null

        // when
        val result = useCase(sessionId = 1L, parentIndex = 1)

        // then
        assertNull(result)
    }
}
