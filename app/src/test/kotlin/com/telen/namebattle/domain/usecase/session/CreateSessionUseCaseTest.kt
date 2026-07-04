package com.telen.namebattle.domain.usecase.session

import com.telen.namebattle.domain.model.Gender
import com.telen.namebattle.domain.repository.SessionRepository
import com.telen.namebattle.util.buildSession
import com.telen.namebattle.util.sha256
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class CreateSessionUseCaseTest {

    private lateinit var sessionRepository: SessionRepository
    private lateinit var useCase: CreateSessionUseCase

    @Before
    fun setUp() {
        sessionRepository = mockk()
        useCase = CreateSessionUseCase(sessionRepository)
    }

    @Test
    fun `hashes parent1 password before calling repository`() = runTest {
        // given
        val plainPassword = "parent1secret"
        val expectedSession = buildSession()
        val capturedHash = slot<String>()
        coEvery {
            sessionRepository.createSession(
                gender = any(),
                parent1Name = any(),
                parent1PasswordHash = capture(capturedHash),
                parent2Name = any(),
                parent2PasswordHash = any(),
            )
        } returns expectedSession

        // when
        useCase(
            CreateSessionUseCase.Params(
                gender = Gender.BOY,
                parent1Name = "Alice",
                parent1Password = plainPassword,
                parent2Name = null,
                parent2Password = null,
            )
        )

        // then
        assertEquals(plainPassword.sha256(), capturedHash.captured)
    }

    @Test
    fun `hashes parent2 password when parent2 is present`() = runTest {
        // given
        val parent2Password = "parent2secret"
        val expectedSession = buildSession()
        val capturedHash = slot<String>()
        coEvery {
            sessionRepository.createSession(
                gender = any(),
                parent1Name = any(),
                parent1PasswordHash = any(),
                parent2Name = any(),
                parent2PasswordHash = capture(capturedHash),
            )
        } returns expectedSession

        // when
        useCase(
            CreateSessionUseCase.Params(
                gender = Gender.GIRL,
                parent1Name = "Alice",
                parent1Password = "p1pass",
                parent2Name = "Bob",
                parent2Password = parent2Password,
            )
        )

        // then
        assertEquals(parent2Password.sha256(), capturedHash.captured)
    }

    @Test
    fun `passes null parent2PasswordHash when parent2 password is null`() = runTest {
        // given
        val expectedSession = buildSession()
        coEvery {
            sessionRepository.createSession(
                gender = Gender.BOY,
                parent1Name = "Alice",
                parent1PasswordHash = "p1pass".sha256(),
                parent2Name = null,
                parent2PasswordHash = null,
            )
        } returns expectedSession

        // when
        val result = useCase(
            CreateSessionUseCase.Params(
                gender = Gender.BOY,
                parent1Name = "Alice",
                parent1Password = "p1pass",
                parent2Name = null,
                parent2Password = null,
            )
        )

        // then — if null wasn't forwarded, coEvery wouldn't match and result would fail
        assertEquals(expectedSession, result)
    }

    @Test
    fun `returns the session from repository`() = runTest {
        // given
        val expectedSession = buildSession(id = 42L)
        coEvery {
            sessionRepository.createSession(
                gender = any(),
                parent1Name = any(),
                parent1PasswordHash = any(),
                parent2Name = any(),
                parent2PasswordHash = any(),
            )
        } returns expectedSession

        // when
        val result = useCase(
            CreateSessionUseCase.Params(
                gender = Gender.BOY,
                parent1Name = "Alice",
                parent1Password = "pass",
                parent2Name = null,
                parent2Password = null,
            )
        )

        // then
        assertEquals(expectedSession, result)
    }
}
