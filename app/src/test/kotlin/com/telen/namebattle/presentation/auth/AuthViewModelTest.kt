package com.telen.namebattle.presentation.auth

import com.telen.namebattle.domain.usecase.auth.AuthenticateParentUseCase
import com.telen.namebattle.domain.usecase.firstname.GetShortlistIdsUseCase
import com.telen.namebattle.domain.usecase.session.GetParentUseCase
import com.telen.namebattle.domain.usecase.session.GetSessionUseCase
import com.telen.namebattle.util.buildParent
import com.telen.namebattle.util.buildSession
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val sessionId = 1L

    private lateinit var getSession: GetSessionUseCase
    private lateinit var getParent: GetParentUseCase
    private lateinit var getShortlistIds: GetShortlistIdsUseCase
    private lateinit var authenticate: AuthenticateParentUseCase

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        getSession = mockk()
        getParent = mockk()
        getShortlistIds = mockk()
        authenticate = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): AuthViewModel = AuthViewModel(
        sessionId = sessionId,
        getSession = getSession,
        getParent = getParent,
        getShortlistIds = getShortlistIds,
        authenticate = authenticate,
    )

    @Test
    fun `load populates parents with shortlist counts`() = runTest {
        // given
        val p1 = buildParent(id = 1L, name = "Alice", parentIndex = 0)
        val p2 = buildParent(id = 2L, name = "Bob", parentIndex = 1)
        val session = buildSession(id = sessionId, parent1 = p1, parent2 = p2)
        coEvery { getSession(sessionId) } returns session
        coEvery { getShortlistIds(1L) } returns listOf(10L, 20L)
        coEvery { getShortlistIds(2L) } returns listOf(30L)

        // when
        val vm = createViewModel()
        advanceUntilIdle()

        // then
        val state = vm.state.first()
        assertFalse(state.isLoading)
        assertEquals(2, state.parents.size)
        assertEquals("Alice", state.parents[0].name)
        assertEquals(2, state.parents[0].shortlistCount)
        assertEquals("Bob", state.parents[1].name)
        assertEquals(1, state.parents[1].shortlistCount)
        assertEquals(3, state.totalNames)
    }

    @Test
    fun `onSelectParent sets selectedParentIndex and clears password`() = runTest {
        // given
        val p1 = buildParent(id = 1L, name = "Alice", parentIndex = 0)
        val session = buildSession(id = sessionId, parent1 = p1)
        coEvery { getSession(sessionId) } returns session
        coEvery { getShortlistIds(any()) } returns listOf(1L)
        val vm = createViewModel()
        advanceUntilIdle()
        vm.onPasswordChange("somepass")

        // when
        vm.onSelectParent(0)

        // then
        val state = vm.state.first()
        assertEquals(0, state.selectedParentIndex)
        assertEquals("", state.password)
        assertNull(state.error)
    }

    @Test
    fun `onPasswordChange updates password and clears error`() = runTest {
        // given
        val p1 = buildParent(id = 1L, name = "Alice", parentIndex = 0)
        val session = buildSession(id = sessionId, parent1 = p1)
        coEvery { getSession(sessionId) } returns session
        coEvery { getShortlistIds(any()) } returns listOf(1L)
        coEvery { authenticate(any(), any(), any()) } returns false
        val vm = createViewModel()
        advanceUntilIdle()
        vm.onSelectParent(0)
        vm.onPasswordChange("wrongpass")
        vm.submit()
        advanceUntilIdle()
        // state has an error now

        // when
        vm.onPasswordChange("newpass")

        // then
        val state = vm.state.first()
        assertEquals("newpass", state.password)
        assertNull(state.error)
    }

    @Test
    fun `submit does nothing when no parent selected`() = runTest {
        // given
        val p1 = buildParent(id = 1L, name = "Alice", parentIndex = 0)
        val session = buildSession(id = sessionId, parent1 = p1)
        coEvery { getSession(sessionId) } returns session
        coEvery { getShortlistIds(any()) } returns listOf(1L)
        val vm = createViewModel()
        advanceUntilIdle()
        // selectedParentIndex is null by default

        // when
        vm.submit()
        advanceUntilIdle()

        // then
        assertFalse(vm.state.first().isChecking)
    }

    @Test
    fun `submit does nothing when password is blank`() = runTest {
        // given
        val p1 = buildParent(id = 1L, name = "Alice", parentIndex = 0)
        val session = buildSession(id = sessionId, parent1 = p1)
        coEvery { getSession(sessionId) } returns session
        coEvery { getShortlistIds(any()) } returns listOf(1L)
        val vm = createViewModel()
        advanceUntilIdle()
        vm.onSelectParent(0)
        // password is "" by default

        // when
        vm.submit()
        advanceUntilIdle()

        // then
        assertFalse(vm.state.first().isChecking)
    }

    @Test
    fun `submit emits Authenticated event on success`() = runTest {
        // given
        val p1 = buildParent(id = 1L, name = "Alice", parentIndex = 0)
        val session = buildSession(id = sessionId, parent1 = p1)
        coEvery { getSession(sessionId) } returns session
        coEvery { getShortlistIds(any()) } returns listOf(1L)
        coEvery { authenticate(sessionId, 0, "secret") } returns true
        val vm = createViewModel()
        advanceUntilIdle()
        vm.onSelectParent(0)
        vm.onPasswordChange("secret")

        val events = mutableListOf<AuthUiEvent>()
        val job = launch { vm.events.collect { events.add(it) } }

        // when
        vm.submit()
        advanceUntilIdle()

        // then
        assertEquals(AuthUiEvent.Authenticated(0), events.first())
        job.cancel()
    }

    @Test
    fun `submit sets error on wrong password`() = runTest {
        // given
        val p1 = buildParent(id = 1L, name = "Alice", parentIndex = 0)
        val session = buildSession(id = sessionId, parent1 = p1)
        coEvery { getSession(sessionId) } returns session
        coEvery { getShortlistIds(any()) } returns listOf(1L)
        coEvery { authenticate(sessionId, 0, "wrongpass") } returns false
        val vm = createViewModel()
        advanceUntilIdle()
        vm.onSelectParent(0)
        vm.onPasswordChange("wrongpass")

        // when
        vm.submit()
        advanceUntilIdle()

        // then
        val state = vm.state.first()
        assertEquals("Mot de passe incorrect", state.error)
        assertFalse(state.isChecking)
    }

    @Test
    fun `onLaunchBattle emits LaunchBattle event`() = runTest {
        // given
        val p1 = buildParent(id = 1L, name = "Alice", parentIndex = 0)
        val session = buildSession(id = sessionId, parent1 = p1)
        coEvery { getSession(sessionId) } returns session
        coEvery { getShortlistIds(any()) } returns listOf(1L)
        val vm = createViewModel()
        advanceUntilIdle()

        val events = mutableListOf<AuthUiEvent>()
        val job = launch { vm.events.collect { events.add(it) } }

        // when
        vm.onLaunchBattle()
        advanceUntilIdle()

        // then
        assertEquals(AuthUiEvent.LaunchBattle, events.first())
        job.cancel()
    }

    @Test
    fun `resetSelection clears selection and reloads`() = runTest {
        // given
        val p1 = buildParent(id = 1L, name = "Alice", parentIndex = 0)
        val session = buildSession(id = sessionId, parent1 = p1)
        coEvery { getSession(sessionId) } returns session
        coEvery { getShortlistIds(any()) } returns listOf(1L)
        val vm = createViewModel()
        advanceUntilIdle()
        vm.onSelectParent(0)
        vm.onPasswordChange("mypassword")

        // when
        vm.resetSelection()
        advanceUntilIdle()

        // then
        val state = vm.state.first()
        assertNull(state.selectedParentIndex)
        assertEquals("", state.password)
        assertNull(state.error)
        assertFalse(state.isChecking)
    }
}
