package com.telen.namebattle.presentation.results

import com.telen.namebattle.domain.model.BattleRound
import com.telen.namebattle.domain.model.BattleState
import com.telen.namebattle.domain.model.DuelState
import com.telen.namebattle.domain.model.FirstName
import com.telen.namebattle.domain.model.Gender
import com.telen.namebattle.domain.model.Parent
import com.telen.namebattle.domain.model.Session
import com.telen.namebattle.domain.usecase.battle.GetBattleStateUseCase
import com.telen.namebattle.domain.usecase.export.ExportBattleReportUseCase
import com.telen.namebattle.domain.usecase.firstname.GetNamesByIdsUseCase
import com.telen.namebattle.domain.usecase.session.GetSessionUseCase
import java.io.File
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
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

class ResultsViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val getSession = mockk<GetSessionUseCase>()
    private val getBattleState = mockk<GetBattleStateUseCase>()
    private val getNamesByIds = mockk<GetNamesByIdsUseCase>()
    private val exportBattleReport = mockk<ExportBattleReportUseCase>(relaxed = true)

    private val sessionId = 1L

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun parent(id: Long = 1L) = Parent(
        id = id, sessionId = sessionId, name = "Alice", passwordHash = "hash", parentIndex = 0
    )

    private fun session() = Session(
        id = sessionId, gender = Gender.BOY, parent1 = parent(), parent2 = null
    )

    private fun firstName(id: Long, name: String) = FirstName(
        id = id, name = name, gender = Gender.BOY,
        birthsSince1900 = 0, birthsSince1980 = 0, birthsSince2000 = 0, birthsSince2010 = 0
    )

    private fun battleState(finalists: List<Long>, roundCount: Int = 2): BattleState {
        val duel = DuelState(firstName1Id = finalists.firstOrNull() ?: 1L, firstName2Id = null, winnerId = finalists.firstOrNull())
        val rounds = (1..roundCount).map { BattleRound(roundNumber = it, duels = listOf(duel)) }
        return BattleState(
            sessionId = sessionId,
            initialCount = finalists.size,
            targetFinalists = finalists.size,
            rounds = rounds,
            finalists = finalists,
            isComplete = true
        )
    }

    private fun makeViewModel() = ResultsViewModel(
        sessionId = sessionId,
        getSession = getSession,
        getBattleState = getBattleState,
        getNamesByIds = getNamesByIds,
        exportBattleReport = exportBattleReport
    )

    @Test
    fun `load populates finalists with resolved names`() = runTest {
        // given
        coEvery { getSession(sessionId) } returns session()
        coEvery { getBattleState(sessionId) } returns battleState(finalists = listOf(10L, 20L))
        coEvery { getNamesByIds(listOf(10L, 20L)) } returns listOf(firstName(10L, "Emma"), firstName(20L, "Leo"))

        // when
        val vm = makeViewModel()
        advanceUntilIdle()

        // then
        val state = vm.state.value
        assertFalse(state.isLoading)
        assertEquals(listOf("Emma", "Leo"), state.finalists)
    }

    @Test
    fun `load sets roundsPlayed from battle rounds size`() = runTest {
        // given
        coEvery { getSession(sessionId) } returns session()
        coEvery { getBattleState(sessionId) } returns battleState(finalists = listOf(10L), roundCount = 3)
        coEvery { getNamesByIds(any()) } returns listOf(firstName(10L, "Emma"))

        // when
        val vm = makeViewModel()
        advanceUntilIdle()

        // then
        assertEquals(3, vm.state.value.roundsPlayed)
    }

    @Test
    fun `load handles null session gracefully`() = runTest {
        // given
        coEvery { getSession(sessionId) } returns null

        // when
        val vm = makeViewModel()
        advanceUntilIdle()

        // then
        val state = vm.state.value
        assertFalse(state.isLoading)
        assertTrue(state.finalists.isEmpty())
    }

    @Test
    fun `load handles null battle state gracefully`() = runTest {
        // given
        coEvery { getSession(sessionId) } returns session()
        coEvery { getBattleState(sessionId) } returns null

        // when
        val vm = makeViewModel()
        advanceUntilIdle()

        // then
        val state = vm.state.value
        assertFalse(state.isLoading)
        assertTrue(state.finalists.isEmpty())
        assertEquals(0, state.roundsPlayed)
    }

    // ── onExportPdf ───────────────────────────────────────────────────────────

    @Test
    fun `onExportPdf calls export use case and resets isExporting`() = runTest {
        coEvery { getSession(sessionId) } returns null
        coEvery { exportBattleReport(sessionId) } returns mockk<File>()
        val vm = makeViewModel()
        advanceUntilIdle()

        vm.onExportPdf()
        // Allow the real IO dispatcher to complete before asserting
        Thread.sleep(100)
        advanceUntilIdle()

        coVerify(exactly = 1) { exportBattleReport(sessionId) }
        assertFalse(vm.state.value.isExporting)
    }

    @Test
    fun `onExportPdf does not call use case when already exporting`() = runTest {
        coEvery { getSession(sessionId) } returns null
        coEvery { exportBattleReport(sessionId) } returns mockk<File>()
        val vm = makeViewModel()
        advanceUntilIdle()

        vm.onExportPdf()
        vm.onExportPdf()
        Thread.sleep(100)
        advanceUntilIdle()

        coVerify(exactly = 1) { exportBattleReport(sessionId) }
    }

    @Test
    fun `onExportPdf sends SharePdf effect for non-null result`() = runTest {
        coEvery { getSession(sessionId) } returns null
        val fakeFile = mockk<File>()
        coEvery { exportBattleReport(sessionId) } returns fakeFile
        val vm = makeViewModel()
        advanceUntilIdle()

        vm.onExportPdf()
        Thread.sleep(100)

        val effect = vm.effects.first() as? ResultsEffect.SharePdf
        assertEquals(fakeFile, effect?.file)
    }

    @Test
    fun `load handles multiple finalists correctly`() = runTest {
        // given
        val ids = listOf(10L, 20L, 30L, 40L)
        coEvery { getSession(sessionId) } returns session()
        coEvery { getBattleState(sessionId) } returns battleState(finalists = ids, roundCount = 4)
        coEvery { getNamesByIds(ids) } returns ids.mapIndexed { i, id -> firstName(id, "Name$i") }

        // when
        val vm = makeViewModel()
        advanceUntilIdle()

        // then
        assertEquals(4, vm.state.value.finalists.size)
    }
}
