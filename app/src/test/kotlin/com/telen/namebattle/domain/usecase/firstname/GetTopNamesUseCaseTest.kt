package com.telen.namebattle.domain.usecase.firstname

import com.telen.namebattle.domain.model.Gender
import com.telen.namebattle.domain.repository.FirstNameRepository
import com.telen.namebattle.util.buildFirstName
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GetTopNamesUseCaseTest {

    private lateinit var firstNameRepository: FirstNameRepository
    private lateinit var useCase: GetTopNamesUseCase

    @Before
    fun setUp() {
        firstNameRepository = mockk()
        useCase = GetTopNamesUseCase(firstNameRepository)
    }

    @Test
    fun `delegates to repo getTopNames and returns the flow`() = runTest {
        // given
        val expectedNames = listOf(buildFirstName(id = 1L), buildFirstName(id = 2L))
        every {
            firstNameRepository.getTopNames(gender = Gender.GIRL, fromYear = 2000)
        } returns flowOf(expectedNames)

        // when
        val result = useCase(gender = Gender.GIRL, fromYear = 2000).first()

        // then
        assertEquals(expectedNames, result)
        verify(exactly = 1) {
            firstNameRepository.getTopNames(gender = Gender.GIRL, fromYear = 2000)
        }
    }
}
