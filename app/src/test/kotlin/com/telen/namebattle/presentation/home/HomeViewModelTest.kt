package com.telen.namebattle.presentation.home

import com.telen.namebattle.data.local.DatabaseSeeder
import com.telen.namebattle.domain.usecase.battle.ClearBattleStateUseCase
import com.telen.namebattle.domain.usecase.battle.GetBattleStateUseCase
import com.telen.namebattle.domain.usecase.export.ExportBattleReportUseCase
import com.telen.namebattle.domain.usecase.firstname.GetShortlistIdsUseCase
import com.telen.namebattle.domain.usecase.session.DeleteSessionUseCase
import com.telen.namebattle.domain.usecase.session.GetAllSessionsUseCase
import com.telen.namebattle.util.buildBattleState
import com.telen.namebattle.util.buildParent
import com.telen.namebattle.util.buildSession
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.mockk
import java.io.File
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
class HomeViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var getAllSessions: GetAllSessionsUseCase
    private lateinit var deleteSession: DeleteSessionUseCase
    private lateinit var getShortlistIds: GetShortlistIdsUseCase
    private lateinit var getBattleState: GetBattleStateUseCase
    private lateinit var clearBattleState: ClearBattleStateUseCase
    private lateinit var exportBattleReport: ExportBattleReportUseCase
    private lateinit var seeder: DatabaseSeeder

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        getAllSessions = mockk()
        deleteSession = mockk()
        getShortlistIds = mockk()
        getBattleState = mockk()
        clearBattleState = mockk()
        exportBattleReport = mockk(relaxed = true)
        seeder = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): HomeViewModel = HomeViewModel(
        getAllSessions = getAllSessions,
        deleteSession = deleteSession,
        getShortlistIds = getShortlistIds,
        getBattleState = getBattleState,
        clearBattleState = clearBattleState,
        exportBattleReport = exportBattleReport,
        seeder = seeder,
    )

    @Test
    fun `load populates sessions with correct battle status NOT_STARTED when no battle state`()
    = runTest {
        // given
        val session = buildSession(id = 1L)
        coEvery { getAllSessions() } returns listOf(session)
        coEvery { getShortlistIds(any()) } returns listOf(1L, 2L)
        coEvery { getBattleState(1L) } returns null

        // when
        val vm = createViewModel()
        advanceUntilIdle()

        // then
        val state = vm.state.first()
        assertEquals(1, state.sessions.size)
        assertEquals(BattleStatus.NOT_STARTED, state.sessions[0].battleStatus)
    }

    @Test
    fun `load sets battleStatus IN_PROGRESS when battle state exists and not complete`() = runTest {
        // given
        val session = buildSession(id = 1L)
        val battle = buildBattleState(sessionId = 1L, isComplete = false)
        coEvery { getAllSessions() } returns listOf(session)
        coEvery { getShortlistIds(any()) } returns listOf(1L)
        coEvery { getBattleState(1L) } returns battle

        // when
        val vm = createViewModel()
        advanceUntilIdle()

        // then
        val state = vm.state.first()
        assertEquals(BattleStatus.IN_PROGRESS, state.sessions[0].battleStatus)
    }

    @Test
    fun `load sets battleStatus COMPLETED when battle state is complete`() = runTest {
        // given
        val session = buildSession(id = 1L)
        val battle = buildBattleState(sessionId = 1L, isComplete = true)
        coEvery { getAllSessions() } returns listOf(session)
        coEvery { getShortlistIds(any()) } returns listOf(1L)
        coEvery { getBattleState(1L) } returns battle

        // when
        val vm = createViewModel()
        advanceUntilIdle()

        // then
        val state = vm.state.first()
        assertEquals(BattleStatus.COMPLETED, state.sessions[0].battleStatus)
    }

    @Test
    fun `load sets canStartBattle true when all parents have names`() = runTest {
        // given
        val p1 = buildParent(id = 1L, parentIndex = 0)
        val p2 = buildParent(id = 2L, parentIndex = 1)
        val session = buildSession(id = 1L, parent1 = p1, parent2 = p2)
        coEvery { getAllSessions() } returns listOf(session)
        coEvery { getShortlistIds(1L) } returns listOf(1L, 2L)
        coEvery { getShortlistIds(2L) } returns listOf(3L, 4L)
        coEvery { getBattleState(1L) } returns null

        // when
        val vm = createViewModel()
        advanceUntilIdle()

        // then
        val state = vm.state.first()
        assertTrue(state.sessions[0].canStartBattle)
    }

    @Test
    fun `load sets canStartBattle false when a parent has empty list`() = runTest {
        // given
        val p1 = buildParent(id = 1L, parentIndex = 0)
        val p2 = buildParent(id = 2L, parentIndex = 1)
        val session = buildSession(id = 1L, parent1 = p1, parent2 = p2)
        coEvery { getAllSessions() } returns listOf(session)
        coEvery { getShortlistIds(1L) } returns listOf(1L)
        coEvery { getShortlistIds(2L) } returns emptyList()
        coEvery { getBattleState(1L) } returns null

        // when
        val vm = createViewModel()
        advanceUntilIdle()

        // then
        val state = vm.state.first()
        assertFalse(state.sessions[0].canStartBattle)
    }

    @Test
    fun `onCreateSession emits NavigateToSetup event`() = runTest {
        // given
        coEvery { getAllSessions() } returns emptyList()
        val vm = createViewModel()
        advanceUntilIdle()

        val events = mutableListOf<HomeUiEvent>()
        val job = launch { vm.events.collect { events.add(it) } }

        // when
        vm.onCreateSession()
        advanceUntilIdle()

        // then
        assertEquals(HomeUiEvent.NavigateToSetup, events.first())
        job.cancel()
    }

    @Test
    fun `onDeleteSession sets pendingDeleteSessionId`() = runTest {
        // given
        coEvery { getAllSessions() } returns emptyList()
        val vm = createViewModel()
        advanceUntilIdle()

        // when
        vm.onDeleteSession(42L)

        // then
        assertEquals(42L, vm.state.first().pendingDeleteSessionId)
    }

    @Test
    fun `onDeleteDismissed clears pendingDeleteSessionId`() = runTest {
        // given
        coEvery { getAllSessions() } returns emptyList()
        val vm = createViewModel()
        advanceUntilIdle()
        vm.onDeleteSession(42L)

        // when
        vm.onDeleteDismissed()

        // then
        assertNull(vm.state.first().pendingDeleteSessionId)
    }

    @Test
    fun `onDeleteConfirmed deletes session and removes it from list`() = runTest {
        // given
        val session = buildSession(id = 5L)
        coEvery { getAllSessions() } returns listOf(session)
        coEvery { getShortlistIds(any()) } returns listOf(1L)
        coEvery { getBattleState(any()) } returns null
        coJustRun { deleteSession(5L) }
        val vm = createViewModel()
        advanceUntilIdle()

        vm.onDeleteSession(5L)

        // when
        vm.onDeleteConfirmed()
        advanceUntilIdle()

        // then
        coVerify(exactly = 1) { deleteSession(5L) }
        val state = vm.state.first()
        assertTrue(state.sessions.none { it.sessionId == 5L })
        assertNull(state.pendingDeleteSessionId)
    }

    @Test
    fun `onExportPdf sets isExportingSessionId then clears it and emits SharePdf event`() =
        runTest {
            // given
            coEvery { getAllSessions() } returns emptyList()
            val fakeFile = mockk<File>()
            coEvery { exportBattleReport(42L) } returns fakeFile
            val vm = createViewModel()
            advanceUntilIdle()

            val events = mutableListOf<HomeUiEvent>()
            val job = launch { vm.events.collect { events.add(it) } }

            // when
            vm.onExportPdf(42L)
            advanceUntilIdle()

            // then
            val shareEvent = events.filterIsInstance<HomeUiEvent.SharePdf>().firstOrNull()
            assertEquals(fakeFile, shareEvent?.file)
            assertEquals(null, vm.state.value.isExportingSessionId)
            job.cancel()
        }

    @Test
    fun `onExportPdf does nothing when another export is already running`() = runTest {
        // given
        coEvery { getAllSessions() } returns emptyList()
        coEvery { exportBattleReport(any()) } returns mockk()
        val vm = createViewModel()
        advanceUntilIdle()

        vm.onExportPdf(1L)
        // second call while first might still be in progress
        vm.onExportPdf(2L)
        advanceUntilIdle()

        // only one export should have run
        coVerify(exactly = 1) { exportBattleReport(any()) }
    }

    @Test
    fun `onRestartBattle clears battle state and calls onDone`() = runTest {
        // given
        val session = buildSession(id = 3L)
        val battle = buildBattleState(sessionId = 3L, isComplete = true)
        coEvery { getAllSessions() } returns listOf(session)
        coEvery { getShortlistIds(any()) } returns listOf(1L)
        coEvery { getBattleState(3L) } returns battle
        coJustRun { clearBattleState(3L) }
        val vm = createViewModel()
        advanceUntilIdle()

        var doneCalled = false

        // when
        vm.onRestartBattle(3L) { doneCalled = true }
        advanceUntilIdle()

        // then
        coVerify(exactly = 1) { clearBattleState(3L) }
        assertTrue(doneCalled)
        val state = vm.state.first()
        assertEquals(BattleStatus.NOT_STARTED, state.sessions[0].battleStatus)
    }
}
