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

class SearchFreeTextUseCaseTest {

    private lateinit var firstNameRepository: FirstNameRepository
    private lateinit var useCase: SearchFreeTextUseCase

    @Before
    fun setUp() {
        firstNameRepository = mockk()
        useCase = SearchFreeTextUseCase(firstNameRepository)
    }

    @Test
    fun `delegates to repo searchFreeText and returns the flow`() = runTest {
        // given
        val expectedNames = listOf(buildFirstName(id = 1L, name = "Emma"))
        every { firstNameRepository.searchFreeText(query = "em", gender = Gender.GIRL) } returns flowOf(expectedNames)

        // when
        val result = useCase(query = "em", gender = Gender.GIRL).first()

        // then
        assertEquals(expectedNames, result)
        verify(exactly = 1) { firstNameRepository.searchFreeText(query = "em", gender = Gender.GIRL) }
    }
}
