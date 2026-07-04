package com.telen.namebattle.domain.usecase.firstname

import com.telen.namebattle.domain.repository.FirstNameRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GetNamesWithMeaningUseCaseTest {

    private lateinit var firstNameRepository: FirstNameRepository
    private lateinit var useCase: GetNamesWithMeaningUseCase

    @Before
    fun setUp() {
        firstNameRepository = mockk()
        useCase = GetNamesWithMeaningUseCase(firstNameRepository)
    }

    @Test
    fun `delegates to repo namesWithMeaning and returns the set`() = runTest {
        // given
        val expectedSet = setOf("Emma", "Liam", "Olivia")
        coEvery { firstNameRepository.namesWithMeaning() } returns expectedSet

        // when
        val result = useCase()

        // then
        assertEquals(expectedSet, result)
        coVerify(exactly = 1) { firstNameRepository.namesWithMeaning() }
    }
}
