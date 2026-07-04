package com.telen.namebattle.domain.usecase.firstname

import com.telen.namebattle.domain.repository.SessionRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GetShortlistIdsUseCaseTest {

    private lateinit var sessionRepository: SessionRepository
    private lateinit var useCase: GetShortlistIdsUseCase

    @Before
    fun setUp() {
        sessionRepository = mockk()
        useCase = GetShortlistIdsUseCase(sessionRepository)
    }

    @Test
    fun `delegates to repo getShortlistIds and returns the list`() = runTest {
        // given
        val expectedIds = listOf(10L, 20L, 30L)
        coEvery { sessionRepository.getShortlistIds(parentId = 1L) } returns expectedIds

        // when
        val result = useCase(parentId = 1L)

        // then
        assertEquals(expectedIds, result)
        coVerify(exactly = 1) { sessionRepository.getShortlistIds(parentId = 1L) }
    }
}
