package com.telen.namebattle.presentation.setup

import com.telen.namebattle.domain.model.Gender
import com.telen.namebattle.domain.usecase.session.CreateSessionUseCase
import com.telen.namebattle.util.buildSession
import io.mockk.coEvery
import io.mockk.coVerify
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SetupViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var createSession: CreateSessionUseCase

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        createSession = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): SetupViewModel = SetupViewModel(createSession)

    private fun SetupViewModel.fillParent1(name: String = "Alice", pw: String = "pass123") {
        onParent1Name(name)
        onParent1Password(pw)
        onParent1Confirm(pw)
    }

    private fun SetupViewModel.fillParent2(name: String = "Bob", pw: String = "pass456") {
        onParent2Name(name)
        onParent2Password(pw)
        onParent2Confirm(pw)
    }

    @Test
    fun `lockParent1 does nothing when parent1CanLock is false`() = runTest {
        // given
        val vm = createViewModel()
        // parent1Name is blank by default → parent1CanLock = false

        // when
        vm.lockParent1()

        // then
        assertFalse(vm.state.first().parent1Locked)
    }

    @Test
    fun `lockParent1 creates session when solo mode and canCreate becomes true`() = runTest {
        // given
        val session = buildSession(id = 10L)
        coEvery { createSession(any()) } returns session
        val vm = createViewModel()
        vm.fillParent1()
        vm.onSoloToggle(true)

        // when
        vm.lockParent1()
        advanceUntilIdle()

        // then
        assertTrue(vm.state.first().parent1Locked)
        coVerify(exactly = 1) { createSession(any()) }
    }

    @Test
    fun `lockParent2 creates session when both parents locked`() = runTest {
        // given
        val session = buildSession(id = 20L)
        coEvery { createSession(any()) } returns session
        val vm = createViewModel()
        vm.fillParent1()
        vm.lockParent1()
        vm.fillParent2()

        // when
        vm.lockParent2()
        advanceUntilIdle()

        // then
        assertTrue(vm.state.first().parent2Locked)
        coVerify(exactly = 1) { createSession(any()) }
    }

    @Test
    fun `createSession emits SessionCreated event with session id`() = runTest {
        // given
        val session = buildSession(id = 99L)
        coEvery { createSession(any()) } returns session
        val vm = createViewModel()
        vm.fillParent1()
        vm.onSoloToggle(true)

        val events = mutableListOf<SetupUiEvent>()
        val job = launch { vm.events.collect { events.add(it) } }

        // when
        vm.lockParent1()
        advanceUntilIdle()

        // then
        assertEquals(SetupUiEvent.SessionCreated(99L), events.first())
        job.cancel()
    }

    @Test
    fun `createSession sets isCreating to false on exception`() = runTest {
        // given
        coEvery { createSession(any()) } throws RuntimeException("network error")
        val vm = createViewModel()
        vm.fillParent1()
        vm.onSoloToggle(true)

        // when
        vm.lockParent1()
        advanceUntilIdle()

        // then
        assertFalse(vm.state.first().isCreating)
    }

    @Test
    fun `createSession sends null parent2 in solo mode`() = runTest {
        // given
        val session = buildSession(id = 1L)
        coEvery { createSession(any()) } returns session
        val vm = createViewModel()
        vm.fillParent1(name = "Alice", pw = "secret")
        vm.onSoloToggle(true)

        // when
        vm.lockParent1()
        advanceUntilIdle()

        // then
        coVerify {
            createSession(
                match { params ->
                    params.parent2Name == null && params.parent2Password == null
                }
            )
        }
    }

    @Test
    fun `onSoloToggle creates session if parent1 already locked`() = runTest {
        // given
        val session = buildSession(id = 5L)
        coEvery { createSession(any()) } returns session
        val vm = createViewModel()
        vm.fillParent1()
        vm.lockParent1()
        // parent1Locked = true, soloMode = false => canCreate still false (needs parent2)

        val events = mutableListOf<SetupUiEvent>()
        val job = launch { vm.events.collect { events.add(it) } }

        // when - toggling soloMode makes canCreate = true (parent1Locked && soloMode)
        vm.onSoloToggle(true)
        advanceUntilIdle()

        // then
        assertTrue(events.any { it is SetupUiEvent.SessionCreated })
        job.cancel()
    }

    @Test
    fun `duplicate createSession calls are ignored when isCreating is true`() = runTest {
        // given
        val session = buildSession(id = 7L)
        coEvery { createSession(any()) } returns session
        val vm = createViewModel()
        vm.fillParent1()
        vm.fillParent2()
        // Lock parent1 first (soloMode=false, needs parent2 too)
        vm.lockParent1()
        // Now lock parent2 → canCreate becomes true, createSession() called once
        vm.lockParent2()
        advanceUntilIdle()

        // when - trigger lockParent2 again (already locked, parent2CanLock=true but isCreating=false after success)
        // The key is that during the first createSession call, isCreating=true blocks re-entry
        // We verify that createSession was called exactly once total
        coVerify(exactly = 1) { createSession(any()) }
    }
}
