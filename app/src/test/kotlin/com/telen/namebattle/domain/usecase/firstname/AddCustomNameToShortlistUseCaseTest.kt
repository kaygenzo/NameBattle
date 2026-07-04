package com.telen.namebattle.domain.usecase.firstname

import com.telen.namebattle.domain.model.Gender
import com.telen.namebattle.domain.repository.FirstNameRepository
import com.telen.namebattle.domain.repository.SessionRepository
import com.telen.namebattle.util.buildFirstName
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class AddCustomNameToShortlistUseCaseTest {

    private lateinit var firstNameRepository: FirstNameRepository
    private lateinit var sessionRepository: SessionRepository
    private lateinit var useCase: AddCustomNameToShortlistUseCase

    @Before
    fun setUp() {
        firstNameRepository = mockk()
        sessionRepository = mockk()
        useCase = AddCustomNameToShortlistUseCase(firstNameRepository, sessionRepository)
    }

    @Test
    fun `calls getOrCreateCustom then addNameToShortlist with the returned id`() = runTest {
        // given
        val customName = buildFirstName(id = 99L, name = "Zephyr", gender = Gender.BOY)
        coEvery { firstNameRepository.getOrCreateCustom(name = "Zephyr", gender = Gender.BOY) } returns customName
        coEvery { sessionRepository.addNameToShortlist(parentId = 1L, firstNameId = 99L) } returns Unit

        // when
        useCase(parentId = 1L, name = "Zephyr", gender = Gender.BOY)

        // then
        coVerify(exactly = 1) { firstNameRepository.getOrCreateCustom(name = "Zephyr", gender = Gender.BOY) }
        coVerify(exactly = 1) { sessionRepository.addNameToShortlist(parentId = 1L, firstNameId = 99L) }
    }

    @Test
    fun `uses the id from getOrCreateCustom even when name already exists`() = runTest {
        // given
        val existingName = buildFirstName(id = 42L, name = "Emma", gender = Gender.GIRL)
        coEvery { firstNameRepository.getOrCreateCustom(name = "Emma", gender = Gender.GIRL) } returns existingName
        coEvery { sessionRepository.addNameToShortlist(parentId = 2L, firstNameId = 42L) } returns Unit

        // when
        useCase(parentId = 2L, name = "Emma", gender = Gender.GIRL)

        // then
        coVerify(exactly = 1) { sessionRepository.addNameToShortlist(parentId = 2L, firstNameId = 42L) }
    }
}
