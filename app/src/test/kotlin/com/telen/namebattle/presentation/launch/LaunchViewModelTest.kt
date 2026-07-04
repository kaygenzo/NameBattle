package com.telen.namebattle.presentation.launch

import com.telen.namebattle.domain.usecase.battle.StartBattleUseCase
import com.telen.namebattle.domain.usecase.firstname.GetShortlistIdsUseCase
import com.telen.namebattle.domain.usecase.session.GetSessionUseCase
import com.telen.namebattle.util.buildBattleState
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LaunchViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val sessionId = 1L

    private lateinit var getSession: GetSessionUseCase
    private lateinit var getShortlistIds: GetShortlistIdsUseCase
    private lateinit var startBattle: StartBattleUseCase

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        getSession = mockk()
        getShortlistIds = mockk()
        startBattle = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): LaunchViewModel = LaunchViewModel(
        sessionId = sessionId,
        getSession = getSession,
        getShortlistIds = getShortlistIds,
        startBattle = startBattle,
    )

    @Test
    fun `load populates state with session data`() = runTest {
        // given
        val p1 = buildParent(id = 1L, name = "Alice", parentIndex = 0)
        val session = buildSession(id = sessionId, parent1 = p1)
        coEvery { getSession(sessionId) } returns session
        coEvery { getShortlistIds(1L) } returns listOf(1L, 2L, 3L)

        // when
        val vm = createViewModel()
        advanceUntilIdle()

        // then
        val state = vm.state.first()
        assertFalse(state.isLoading)
        assertEquals("Alice", state.parent1Name)
        assertEquals(3, state.parent1Count)
        assertEquals(3, state.total)
    }

    @Test
    fun `load handles session with 2 parents`() = runTest {
        // given
        val p1 = buildParent(id = 1L, name = "Alice", parentIndex = 0)
        val p2 = buildParent(id = 2L, name = "Bob", parentIndex = 1)
        val session = buildSession(id = sessionId, parent1 = p1, parent2 = p2)
        coEvery { getSession(sessionId) } returns session
        coEvery { getShortlistIds(1L) } returns listOf(10L, 20L)
        coEvery { getShortlistIds(2L) } returns listOf(30L, 40L, 50L)

        // when
        val vm = createViewModel()
        advanceUntilIdle()

        // then
        val state = vm.state.first()
        assertEquals("Bob", state.parent2Name)
        assertEquals(2, state.parent1Count)
        assertEquals(3, state.parent2Count)
        assertEquals(5, state.total)
    }

    @Test
    fun `roundsEstimate is 1 when total is 4 or less`() = runTest {
        // given
        val p1 = buildParent(id = 1L, name = "Alice", parentIndex = 0)
        val session = buildSession(id = sessionId, parent1 = p1)
        coEvery { getSession(sessionId) } returns session
        coEvery { getShortlistIds(1L) } returns listOf(1L, 2L, 3L, 4L)

        // when
        val vm = createViewModel()
        advanceUntilIdle()

        // then
        assertEquals(1, vm.state.first().roundsEstimate)
    }

    @Test
    fun `roundsEstimate uses log2 formula when total exceeds 4`() = runTest {
        // given - 8 names total: ceil(log2(8/4)) + 1 = ceil(1.0) + 1 = 2
        val p1 = buildParent(id = 1L, name = "Alice", parentIndex = 0)
        val session = buildSession(id = sessionId, parent1 = p1)
        coEvery { getSession(sessionId) } returns session
        coEvery { getShortlistIds(1L) } returns (1L..8L).toList()

        // when
        val vm = createViewModel()
        advanceUntilIdle()

        // then
        assertEquals(2, vm.state.first().roundsEstimate)
    }

    @Test
    fun `start emits BattleStarted event`() = runTest {
        // given
        val p1 = buildParent(id = 1L, name = "Alice", parentIndex = 0)
        val session = buildSession(id = sessionId, parent1 = p1)
        val battle = buildBattleState(sessionId = sessionId)
        coEvery { getSession(sessionId) } returns session
        coEvery { getShortlistIds(1L) } returns listOf(1L, 2L)
        coEvery { startBattle(session) } returns battle
        val vm = createViewModel()
        advanceUntilIdle()

        val events = mutableListOf<LaunchUiEvent>()
        val job = launch { vm.events.collect { events.add(it) } }

        // when
        vm.start()
        advanceUntilIdle()

        // then
        assertEquals(LaunchUiEvent.BattleStarted, events.first())
        job.cancel()
    }

    @Test
    fun `start does nothing when isStarting is already true`() = runTest {
        // given
        val p1 = buildParent(id = 1L, name = "Alice", parentIndex = 0)
        val session = buildSession(id = sessionId, parent1 = p1)
        val battle = buildBattleState(sessionId = sessionId)
        coEvery { getSession(sessionId) } returns session
        coEvery { getShortlistIds(1L) } returns listOf(1L, 2L)
        coEvery { startBattle(session) } returns battle
        val vm = createViewModel()
        advanceUntilIdle()

        val events = mutableListOf<LaunchUiEvent>()
        val job = launch { vm.events.collect { events.add(it) } }

        // when - call start twice rapidly
        vm.start()
        vm.start()
        advanceUntilIdle()

        // then - BattleStarted emitted only once
        assertEquals(1, events.size)
        job.cancel()
    }

    @Test
    fun `load handles null session gracefully`() = runTest {
        // given
        coEvery { getSession(sessionId) } returns null

        // when
        val vm = createViewModel()
        advanceUntilIdle()

        // then
        val state = vm.state.first()
        assertFalse(state.isLoading)
        assertTrue(state.parent1Name.isEmpty())
    }
}
