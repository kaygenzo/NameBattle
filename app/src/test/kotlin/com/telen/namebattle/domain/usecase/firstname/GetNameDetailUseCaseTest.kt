package com.telen.namebattle.domain.usecase.firstname

import com.telen.namebattle.domain.repository.FirstNameRepository
import com.telen.namebattle.util.buildFirstName
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class GetNameDetailUseCaseTest {

    private lateinit var firstNameRepository: FirstNameRepository
    private lateinit var useCase: GetNameDetailUseCase

    @Before
    fun setUp() {
        firstNameRepository = mockk()
        useCase = GetNameDetailUseCase(firstNameRepository)
    }

    @Test
    fun `delegates to repo getNameDetail and returns the result`() = runTest {
        // given
        val expectedName = buildFirstName(id = 5L)
        coEvery { firstNameRepository.getNameDetail(id = 5L) } returns expectedName

        // when
        val result = useCase(id = 5L)

        // then
        assertEquals(expectedName, result)
        coVerify(exactly = 1) { firstNameRepository.getNameDetail(id = 5L) }
    }

    @Test
    fun `returns null when name detail is not found`() = runTest {
        // given
        coEvery { firstNameRepository.getNameDetail(id = 99L) } returns null

        // when
        val result = useCase(id = 99L)

        // then
        assertNull(result)
    }
}
