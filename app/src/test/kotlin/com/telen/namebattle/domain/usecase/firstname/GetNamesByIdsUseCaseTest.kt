package com.telen.namebattle.domain.usecase.firstname

import com.telen.namebattle.domain.repository.FirstNameRepository
import com.telen.namebattle.util.buildFirstName
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GetNamesByIdsUseCaseTest {

    private lateinit var firstNameRepository: FirstNameRepository
    private lateinit var useCase: GetNamesByIdsUseCase

    @Before
    fun setUp() {
        firstNameRepository = mockk()
        useCase = GetNamesByIdsUseCase(firstNameRepository)
    }

    @Test
    fun `delegates to repo getByIds and returns the result`() = runTest {
        // given
        val ids = listOf(1L, 2L, 3L)
        val expectedNames = listOf(
            buildFirstName(id = 1L), buildFirstName(id = 2L), buildFirstName(id = 3L)
        )
        coEvery { firstNameRepository.getByIds(ids = ids) } returns expectedNames

        // when
        val result = useCase(ids = ids)

        // then
        assertEquals(expectedNames, result)
        coVerify(exactly = 1) { firstNameRepository.getByIds(ids = ids) }
    }

    @Test
    fun `returns empty list when no ids provided`() = runTest {
        // given
        coEvery { firstNameRepository.getByIds(ids = emptyList()) } returns emptyList()

        // when
        val result = useCase(ids = emptyList())

        // then
        assertEquals(emptyList<com.telen.namebattle.domain.model.FirstName>(), result)
    }
}
