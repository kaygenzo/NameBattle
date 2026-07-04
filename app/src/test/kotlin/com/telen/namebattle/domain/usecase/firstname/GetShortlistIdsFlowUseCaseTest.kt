package com.telen.namebattle.domain.usecase.firstname

import com.telen.namebattle.domain.repository.SessionRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GetShortlistIdsFlowUseCaseTest {

    private lateinit var sessionRepository: SessionRepository
    private lateinit var useCase: GetShortlistIdsFlowUseCase

    @Before
    fun setUp() {
        sessionRepository = mockk()
        useCase = GetShortlistIdsFlowUseCase(sessionRepository)
    }

    @Test
    fun `delegates to repo getShortlistIdsFlow and returns the flow`() = runTest {
        // given
        val expectedIds = listOf(1L, 2L, 3L)
        every { sessionRepository.getShortlistIdsFlow(parentId = 1L) } returns flowOf(expectedIds)

        // when
        val result = useCase(parentId = 1L).first()

        // then
        assertEquals(expectedIds, result)
        verify(exactly = 1) { sessionRepository.getShortlistIdsFlow(parentId = 1L) }
    }
}
