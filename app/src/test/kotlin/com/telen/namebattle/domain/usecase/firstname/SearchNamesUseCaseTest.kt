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

class SearchNamesUseCaseTest {

    private lateinit var firstNameRepository: FirstNameRepository
    private lateinit var useCase: SearchNamesUseCase

    @Before
    fun setUp() {
        firstNameRepository = mockk()
        useCase = SearchNamesUseCase(firstNameRepository)
    }

    @Test
    fun `delegates to repo searchByFirstLetter and returns the flow`() = runTest {
        // given
        val expectedNames = listOf(buildFirstName(id = 1L, name = "Alice"))
        every {
            firstNameRepository.searchByFirstLetter(letter = 'A', gender = Gender.GIRL)
        } returns flowOf(expectedNames)

        // when
        val result = useCase(letter = 'A', gender = Gender.GIRL).first()

        // then
        assertEquals(expectedNames, result)
        verify(exactly = 1) {
            firstNameRepository.searchByFirstLetter(letter = 'A', gender = Gender.GIRL)
        }
    }
}
