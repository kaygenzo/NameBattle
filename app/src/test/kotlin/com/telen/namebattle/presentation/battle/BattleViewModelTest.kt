package com.telen.namebattle.presentation.battle

import com.telen.namebattle.domain.model.BattleRound
import com.telen.namebattle.domain.model.BattleState
import com.telen.namebattle.domain.model.DuelState
import com.telen.namebattle.domain.model.FirstName
import com.telen.namebattle.domain.model.Gender
import com.telen.namebattle.domain.usecase.battle.ChooseWinnerUseCase
import com.telen.namebattle.domain.usecase.battle.GetBattleStateUseCase
import com.telen.namebattle.domain.usecase.firstname.GetNamesByIdsUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class BattleViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val getBattleState = mockk<GetBattleStateUseCase>()
    private val chooseWinner = mockk<ChooseWinnerUseCase>()
    private val getNamesByIds = mockk<GetNamesByIdsUseCase>()

    private val sessionId = 1L

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun firstName(id: Long, name: String) = FirstName(
        id = id, name = name, gender = Gender.BOY,
        birthsSince1900 = 0, birthsSince1980 = 0, birthsSince2000 = 0, birthsSince2010 = 0
    )

    private fun duelBattleState(
        id1: Long = 10L, id2: Long? = 20L,
        complete: Boolean = false,
        finalists: List<Long> = emptyList()
    ): BattleState {
        val duel = DuelState(firstName1Id = id1, firstName2Id = id2)
        val round = BattleRound(roundNumber = 1, duels = listOf(duel))
        return BattleState(
            sessionId = sessionId,
            initialCount = 2,
            targetFinalists = 2,
            rounds = listOf(round),
            finalists = finalists,
            isComplete = complete
        )
    }

    private fun makeViewModel(): BattleViewModel =
        BattleViewModel(sessionId, getBattleState, chooseWinner, getNamesByIds)

    @Test
    fun `load sets DUEL mode with correct names`() = runTest {
        // given
        val state = duelBattleState(id1 = 10L, id2 = 20L)
        coEvery { getBattleState(sessionId) } returns state
        coEvery { getNamesByIds(any()) } returns listOf(
            firstName(10L, "Emma"), firstName(20L, "Alice")
        )

        // when
        val vm = makeViewModel()
        advanceUntilIdle()

        // then
        val uiState = vm.state.value
        assertEquals(BattleMode.DUEL, uiState.mode)
        assertEquals("Emma", uiState.leftName)
        assertEquals("Alice", uiState.rightName)
        assertEquals(10L, uiState.leftId)
        assertEquals(20L, uiState.rightId)
    }

    @Test
    fun `load sets AUTO_PASS mode when firstName2Id is null`() = runTest {
        // given
        val state = duelBattleState(id1 = 10L, id2 = null)
        coEvery { getBattleState(sessionId) } returns state
        coEvery { getNamesByIds(any()) } returns listOf(firstName(10L, "Emma"))

        // when
        val vm = makeViewModel()
        advanceUntilIdle()

        // then
        val uiState = vm.state.value
        assertEquals(BattleMode.AUTO_PASS, uiState.mode)
        assertEquals("Emma", uiState.autoName)
        assertEquals(10L, uiState.autoId)
    }

    @Test
    fun `choose emits Complete event when battle is complete`() = runTest {
        // given
        val initial = duelBattleState(id1 = 10L, id2 = 20L)
        val completed = duelBattleState(
            id1 = 10L, id2 = 20L, complete = true, finalists = listOf(10L)
        )
        coEvery { getBattleState(sessionId) } returns initial
        coEvery { getNamesByIds(any()) } returns listOf(
            firstName(10L, "Emma"), firstName(20L, "Alice")
        )
        coEvery { chooseWinner(sessionId, 10L) } returns completed

        val vm = makeViewModel()
        advanceUntilIdle()

        val events = mutableListOf<BattleUiEvent>()
        val job = launch { vm.events.collect { events.add(it) } }

        // when
        vm.choose(10L)
        advanceUntilIdle()

        // then
        assertEquals(BattleUiEvent.Complete, events.firstOrNull())
        job.cancel()
    }

    @Test
    fun `choose shows ROUND_SUMMARY when round changes`() = runTest {
        // given
        val duel1 = DuelState(firstName1Id = 10L, firstName2Id = 20L)
        val duel2 = DuelState(firstName1Id = 30L, firstName2Id = 40L)
        val round1 = BattleRound(roundNumber = 1, duels = listOf(duel1))
        val round2 = BattleRound(roundNumber = 2, duels = listOf(duel2))

        val initial = BattleState(sessionId = sessionId, initialCount = 4, targetFinalists = 4,
            rounds = listOf(round1), currentRoundIndex = 0, currentDuelIndex = 0)
        val afterChoice = BattleState(
            sessionId = sessionId, initialCount = 4, targetFinalists = 4,
            rounds = listOf(round1.copy(duels = listOf(duel1.copy(winnerId = 10L))), round2),
            currentRoundIndex = 1, currentDuelIndex = 0
        )

        coEvery { getBattleState(sessionId) } returns initial
        coEvery { getNamesByIds(any()) } returns listOf(
            firstName(10L, "Emma"), firstName(20L, "Alice"),
            firstName(30L, "Leo"), firstName(40L, "Max")
        )
        coEvery { chooseWinner(sessionId, 10L) } returns afterChoice

        val vm = makeViewModel()
        advanceUntilIdle()

        // when
        vm.choose(10L)
        advanceUntilIdle()

        // then
        assertEquals(BattleMode.ROUND_SUMMARY, vm.state.value.mode)
        assertNotNull(vm.state.value.summary)
        assertEquals(1, vm.state.value.summary?.finishedRound)
    }

    @Test
    fun `choose renders next duel when same round`() = runTest {
        // given
        val duel1 = DuelState(firstName1Id = 10L, firstName2Id = 20L)
        val duel2 = DuelState(firstName1Id = 30L, firstName2Id = 40L)
        val round = BattleRound(roundNumber = 1, duels = listOf(duel1, duel2))

        val initial = BattleState(sessionId = sessionId, initialCount = 4, targetFinalists = 4,
            rounds = listOf(round), currentRoundIndex = 0, currentDuelIndex = 0)
        val afterChoice = BattleState(
            sessionId = sessionId, initialCount = 4, targetFinalists = 4,
            rounds = listOf(round.copy(duels = listOf(duel1.copy(winnerId = 10L), duel2))),
            currentRoundIndex = 0, currentDuelIndex = 1
        )

        coEvery { getBattleState(sessionId) } returns initial
        coEvery { getNamesByIds(any()) } returns listOf(
            firstName(10L, "Emma"), firstName(20L, "Alice"),
            firstName(30L, "Leo"), firstName(40L, "Max")
        )
        coEvery { chooseWinner(sessionId, 10L) } returns afterChoice

        val vm = makeViewModel()
        advanceUntilIdle()

        // when
        vm.choose(10L)
        advanceUntilIdle()

        // then
        assertEquals(BattleMode.DUEL, vm.state.value.mode)
        assertEquals("Leo", vm.state.value.leftName)
        assertEquals("Max", vm.state.value.rightName)
    }

    @Test
    fun `continueAfterSummary renders current duel`() = runTest {
        // given
        val duel1 = DuelState(firstName1Id = 10L, firstName2Id = 20L)
        val duel2 = DuelState(firstName1Id = 30L, firstName2Id = 40L)
        val round1 = BattleRound(roundNumber = 1, duels = listOf(duel1))
        val round2 = BattleRound(roundNumber = 2, duels = listOf(duel2))

        val initial = BattleState(sessionId = sessionId, initialCount = 4, targetFinalists = 4,
            rounds = listOf(round1), currentRoundIndex = 0, currentDuelIndex = 0)
        val afterChoice = BattleState(
            sessionId = sessionId, initialCount = 4, targetFinalists = 4,
            rounds = listOf(round1.copy(duels = listOf(duel1.copy(winnerId = 10L))), round2),
            currentRoundIndex = 1, currentDuelIndex = 0
        )

        coEvery { getBattleState(sessionId) } returns initial
        coEvery { getNamesByIds(any()) } returns listOf(
            firstName(10L, "Emma"), firstName(20L, "Alice"),
            firstName(30L, "Leo"), firstName(40L, "Max")
        )
        coEvery { chooseWinner(sessionId, 10L) } returns afterChoice

        val vm = makeViewModel()
        advanceUntilIdle()
        vm.choose(10L)
        advanceUntilIdle()
        assertEquals(BattleMode.ROUND_SUMMARY, vm.state.value.mode)

        // when
        vm.continueAfterSummary()

        // then
        assertEquals(BattleMode.DUEL, vm.state.value.mode)
        assertEquals("Leo", vm.state.value.leftName)
    }

    @Test
    fun `duelKey increments on each renderCurrent`() = runTest {
        // given
        val duel1 = DuelState(firstName1Id = 10L, firstName2Id = 20L)
        val duel2 = DuelState(firstName1Id = 30L, firstName2Id = 40L)
        val round = BattleRound(roundNumber = 1, duels = listOf(duel1, duel2))

        val initial = BattleState(sessionId = sessionId, initialCount = 4, targetFinalists = 4,
            rounds = listOf(round), currentRoundIndex = 0, currentDuelIndex = 0)
        val afterChoice = BattleState(
            sessionId = sessionId, initialCount = 4, targetFinalists = 4,
            rounds = listOf(round.copy(duels = listOf(duel1.copy(winnerId = 10L), duel2))),
            currentRoundIndex = 0, currentDuelIndex = 1
        )

        coEvery { getBattleState(sessionId) } returns initial
        coEvery { getNamesByIds(any()) } returns listOf(
            firstName(10L, "Emma"), firstName(20L, "Alice"),
            firstName(30L, "Leo"), firstName(40L, "Max")
        )
        coEvery { chooseWinner(sessionId, 10L) } returns afterChoice

        val vm = makeViewModel()
        advanceUntilIdle()
        val keyAfterLoad = vm.state.value.duelKey

        // when
        vm.choose(10L)
        advanceUntilIdle()

        // then
        assertEquals(keyAfterLoad + 1, vm.state.value.duelKey)
    }

    @Test
    fun `load does not crash when getBattleState returns null`() = runTest {
        // given
        coEvery { getBattleState(sessionId) } returns null
        coEvery { getNamesByIds(any()) } returns emptyList()

        // when
        val vm = makeViewModel()
        advanceUntilIdle()

        // then — ViewModel stays in loading state without crashing
        assertEquals(true, vm.state.value.isLoading)
    }

    @Test
    fun `showSummary excludes auto-pass duels from eliminated list`() = runTest {
        // given: round1 has 1 normal duel + 1 auto-pass, round2 is the next round
        val normalDuel = DuelState(firstName1Id = 10L, firstName2Id = 20L)
        val autoDuel = DuelState(firstName1Id = 30L, firstName2Id = null)
        val round1 = BattleRound(roundNumber = 1, duels = listOf(normalDuel, autoDuel))
        val round2 = BattleRound(roundNumber = 2, duels = listOf(DuelState(10L, 30L)))

        val initial = BattleState(sessionId = sessionId, initialCount = 3, targetFinalists = 3,
            rounds = listOf(round1), currentRoundIndex = 0, currentDuelIndex = 0)

        // after choosing 10 as winner in first duel, round moves to auto-pass
        val afterNormal = BattleState(sessionId = sessionId, initialCount = 3, targetFinalists = 3,
            rounds = listOf(round1.copy(duels = listOf(normalDuel.copy(winnerId = 10L), autoDuel))),
            currentRoundIndex = 0, currentDuelIndex = 1)

        // after auto-pass of 30, round is complete → new round
        val afterAuto = BattleState(
            sessionId = sessionId, initialCount = 3, targetFinalists = 3,
            rounds = listOf(
                round1.copy(duels = listOf(
                    normalDuel.copy(winnerId = 10L), autoDuel.copy(winnerId = 30L)
                )),
                round2
            ),
            currentRoundIndex = 1, currentDuelIndex = 0
        )

        coEvery { getBattleState(sessionId) } returns initial
        coEvery { getNamesByIds(any()) } returns listOf(
            firstName(10L, "Emma"), firstName(20L, "Alice"), firstName(30L, "Leo")
        )
        coEvery { chooseWinner(sessionId, 10L) } returns afterNormal
        coEvery { chooseWinner(sessionId, 30L) } returns afterAuto

        val vm = makeViewModel()
        advanceUntilIdle()
        vm.choose(10L)
        advanceUntilIdle()
        vm.choose(30L)
        advanceUntilIdle()

        // then — auto-pass winner (Leo) is NOT in eliminated list
        val summary = vm.state.value.summary
        assertNotNull(summary)
        assertEquals(listOf("Alice"), summary?.eliminated)
    }
}
