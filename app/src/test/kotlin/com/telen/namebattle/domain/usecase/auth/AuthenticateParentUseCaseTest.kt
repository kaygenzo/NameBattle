package com.telen.namebattle.domain.usecase.auth

import com.telen.namebattle.domain.repository.SessionRepository
import com.telen.namebattle.util.buildParent
import com.telen.namebattle.util.sha256
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AuthenticateParentUseCaseTest {

    private lateinit var sessionRepository: SessionRepository
    private lateinit var useCase: AuthenticateParentUseCase

    @Before
    fun setUp() {
        sessionRepository = mockk()
        useCase = AuthenticateParentUseCase(sessionRepository)
    }

    @Test
    fun `returns false when parent not found`() = runTest {
        // given
        coEvery { sessionRepository.getParentBySession(sessionId = 1L, parentIndex = 0) } returns null

        // when
        val result = useCase(sessionId = 1L, parentIndex = 0, password = "secret")

        // then
        assertFalse(result)
        coVerify(exactly = 0) { sessionRepository.markParentAuthenticated(any()) }
    }

    @Test
    fun `returns false when password is incorrect and does not call markParentAuthenticated`() = runTest {
        // given
        val parent = buildParent(id = 1L, passwordHash = "correctpassword".sha256())
        coEvery { sessionRepository.getParentBySession(sessionId = 1L, parentIndex = 0) } returns parent

        // when
        val result = useCase(sessionId = 1L, parentIndex = 0, password = "wrongpassword")

        // then
        assertFalse(result)
        coVerify(exactly = 0) { sessionRepository.markParentAuthenticated(any()) }
    }

    @Test
    fun `returns true when password is correct and calls markParentAuthenticated with parent id`() = runTest {
        // given
        val correctPassword = "mySecret"
        val parent = buildParent(id = 42L, passwordHash = correctPassword.sha256())
        coEvery { sessionRepository.getParentBySession(sessionId = 1L, parentIndex = 0) } returns parent
        coEvery { sessionRepository.markParentAuthenticated(parentId = 42L) } returns Unit

        // when
        val result = useCase(sessionId = 1L, parentIndex = 0, password = correctPassword)

        // then
        assertTrue(result)
        coVerify(exactly = 1) { sessionRepository.markParentAuthenticated(parentId = 42L) }
    }
}
