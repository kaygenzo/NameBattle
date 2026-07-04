package com.telen.namebattle.domain.usecase.battle

import com.telen.namebattle.domain.model.BattleRound
import com.telen.namebattle.domain.model.BattleState
import com.telen.namebattle.domain.model.DuelState
import com.telen.namebattle.domain.repository.BattleRepository
import com.telen.namebattle.util.buildBattleState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ChooseWinnerUseCaseTest {

    private lateinit var battleRepository: BattleRepository
    private lateinit var useCase: ChooseWinnerUseCase

    @Before
    fun setUp() {
        battleRepository = mockk()
        useCase = ChooseWinnerUseCase(battleRepository)
    }

    // ── invoke ────────────────────────────────────────────────────────────────

    @Test(expected = IllegalStateException::class)
    fun `throws error when no battle state found for session`() = runTest {
        // given
        coEvery { battleRepository.getBattleState(sessionId = 99L) } returns null

        // when
        useCase(sessionId = 99L, winnerId = 1L)

        // then - exception expected
    }

    @Test
    fun `updates battle state and calls saveBattleState`() = runTest {
        // given
        val initialState = buildBattleState(sessionId = 1L)
        coEvery { battleRepository.getBattleState(sessionId = 1L) } returns initialState
        coEvery { battleRepository.saveBattleState(any()) } returns Unit

        // when
        useCase(sessionId = 1L, winnerId = 1L)

        // then
        coVerify(exactly = 1) { battleRepository.saveBattleState(any()) }
    }

    @Test
    fun `returns the updated battle state after choosing winner`() = runTest {
        // given
        val initialState = buildBattleState(sessionId = 1L, currentDuelIndex = 0)
        coEvery { battleRepository.getBattleState(sessionId = 1L) } returns initialState
        coEvery { battleRepository.saveBattleState(any()) } returns Unit

        // when
        val result = useCase(sessionId = 1L, winnerId = 1L)

        // then
        val expected = useCase.advance(initialState, 1L)
        assertEquals(expected.currentDuelIndex, result.currentDuelIndex)
        assertEquals(expected.currentRoundIndex, result.currentRoundIndex)
    }

    // ── advance ───────────────────────────────────────────────────────────────

    @Test
    fun `advance in middle of round increments duelIndex without creating new round`() {
        // given
        val state = BattleState(
            sessionId = 1L, initialCount = 4, targetFinalists = 4,
            rounds = listOf(
                BattleRound(roundNumber = 1, duels = listOf(
                    DuelState(firstName1Id = 1L, firstName2Id = 2L),
                    DuelState(firstName1Id = 3L, firstName2Id = 4L),
                ))
            ),
            currentRoundIndex = 0, currentDuelIndex = 0,
        )

        // when
        val updated = useCase.advance(state, winnerId = 1L)

        // then
        assertEquals(0, updated.currentRoundIndex)
        assertEquals(1, updated.currentDuelIndex)
        assertEquals(1, updated.rounds.size)
        assertFalse(updated.isComplete)
    }

    @Test
    fun `advance on last duel with 3 winners marks battle as complete`() {
        // given
        val state = BattleState(
            sessionId = 1L, initialCount = 5, targetFinalists = 4,
            rounds = listOf(
                BattleRound(roundNumber = 1, duels = listOf(
                    DuelState(firstName1Id = 1L, firstName2Id = 2L, winnerId = 1L),
                    DuelState(firstName1Id = 3L, firstName2Id = 4L, winnerId = 3L),
                    DuelState(firstName1Id = 5L, firstName2Id = null),
                ))
            ),
            currentRoundIndex = 0, currentDuelIndex = 2,
        )

        // when
        val updated = useCase.advance(state, winnerId = 5L)

        // then
        assertTrue(updated.isComplete)
        assertEquals(listOf(1L, 3L, 5L), updated.finalists)
    }

    @Test
    fun `advance on last duel with exactly 4 winners marks battle as complete`() {
        // given
        val state = BattleState(
            sessionId = 1L, initialCount = 8, targetFinalists = 4,
            rounds = listOf(
                BattleRound(roundNumber = 1, duels = listOf(
                    DuelState(firstName1Id = 1L, firstName2Id = 2L, winnerId = 1L),
                    DuelState(firstName1Id = 3L, firstName2Id = 4L, winnerId = 3L),
                    DuelState(firstName1Id = 5L, firstName2Id = 6L, winnerId = 5L),
                    DuelState(firstName1Id = 7L, firstName2Id = 8L),
                ))
            ),
            currentRoundIndex = 0, currentDuelIndex = 3,
        )

        // when
        val updated = useCase.advance(state, winnerId = 7L)

        // then
        assertTrue(updated.isComplete)
        assertEquals(listOf(1L, 3L, 5L, 7L), updated.finalists)
    }

    @Test
    fun `advance on last duel with 5 winners creates new round`() {
        // given
        val state = BattleState(
            sessionId = 1L, initialCount = 10, targetFinalists = 4,
            rounds = listOf(
                BattleRound(roundNumber = 1, duels = listOf(
                    DuelState(firstName1Id = 1L, firstName2Id = 2L, winnerId = 1L),
                    DuelState(firstName1Id = 3L, firstName2Id = 4L, winnerId = 3L),
                    DuelState(firstName1Id = 5L, firstName2Id = 6L, winnerId = 5L),
                    DuelState(firstName1Id = 7L, firstName2Id = 8L, winnerId = 7L),
                    DuelState(firstName1Id = 9L, firstName2Id = 10L),
                ))
            ),
            currentRoundIndex = 0, currentDuelIndex = 4,
        )

        // when
        val updated = useCase.advance(state, winnerId = 9L)

        // then
        assertFalse(updated.isComplete)
        assertEquals(2, updated.rounds.size)
        assertEquals(1, updated.currentRoundIndex)
        assertEquals(0, updated.currentDuelIndex)
    }
}
