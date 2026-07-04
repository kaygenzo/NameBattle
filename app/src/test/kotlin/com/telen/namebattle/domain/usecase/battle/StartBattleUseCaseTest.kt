package com.telen.namebattle.domain.usecase.battle

import com.telen.namebattle.domain.repository.BattleRepository
import com.telen.namebattle.domain.repository.SessionRepository
import com.telen.namebattle.util.buildParent
import com.telen.namebattle.util.buildSession
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class StartBattleUseCaseTest {

    private lateinit var sessionRepository: SessionRepository
    private lateinit var battleRepository: BattleRepository
    private lateinit var useCase: StartBattleUseCase

    @Before
    fun setUp() {
        sessionRepository = mockk()
        battleRepository = mockk()
        useCase = StartBattleUseCase(sessionRepository, battleRepository)
    }

    // ── invoke ────────────────────────────────────────────────────────────────

    @Test
    fun `merges shortlists of parent1 and parent2 removing duplicates`() = runTest {
        // given
        val parent1 = buildParent(id = 1L, parentIndex = 0)
        val parent2 = buildParent(id = 2L, parentIndex = 1)
        val session = buildSession(id = 1L, parent1 = parent1, parent2 = parent2)
        coEvery { sessionRepository.getShortlistIds(parentId = 1L) } returns listOf(10L, 20L, 30L)
        coEvery { sessionRepository.getShortlistIds(parentId = 2L) } returns listOf(20L, 30L, 40L)
        coEvery { battleRepository.saveBattleState(any()) } returns Unit

        // when
        val result = useCase(session)

        // then
        assertEquals(4, result.initialCount)
        coVerify(exactly = 1) { battleRepository.saveBattleState(any()) }
    }

    @Test
    fun `uses only parent1 shortlist when parent2 is null`() = runTest {
        // given
        val parent1 = buildParent(id = 1L, parentIndex = 0)
        val session = buildSession(id = 1L, parent1 = parent1, parent2 = null)
        coEvery { sessionRepository.getShortlistIds(parentId = 1L) } returns listOf(10L, 20L, 30L)
        coEvery { battleRepository.saveBattleState(any()) } returns Unit

        // when
        val result = useCase(session)

        // then
        assertEquals(3, result.initialCount)
        coVerify(exactly = 0) { sessionRepository.getShortlistIds(parentId = 2L) }
    }

    @Test
    fun `calls saveBattleState with the created battle state`() = runTest {
        // given
        val parent1 = buildParent(id = 1L, parentIndex = 0)
        val session = buildSession(id = 5L, parent1 = parent1, parent2 = null)
        coEvery { sessionRepository.getShortlistIds(parentId = 1L) } returns listOf(1L, 2L, 3L)
        val savedStateSlot = slot<com.telen.namebattle.domain.model.BattleState>()
        coEvery { battleRepository.saveBattleState(capture(savedStateSlot)) } returns Unit

        // when
        val result = useCase(session)

        // then
        assertEquals(result, savedStateSlot.captured)
        assertEquals(5L, savedStateSlot.captured.sessionId)
    }

    @Test
    fun `returns BattleState with correct initialCount from merged shortlists`() = runTest {
        // given
        val parent1 = buildParent(id = 1L, parentIndex = 0)
        val parent2 = buildParent(id = 2L, parentIndex = 1)
        val session = buildSession(id = 1L, parent1 = parent1, parent2 = parent2)
        coEvery { sessionRepository.getShortlistIds(parentId = 1L) } returns listOf(1L, 2L)
        coEvery { sessionRepository.getShortlistIds(parentId = 2L) } returns listOf(3L, 4L)
        coEvery { battleRepository.saveBattleState(any()) } returns Unit

        // when
        val result = useCase(session)

        // then
        assertEquals(4, result.initialCount)
        assertFalse(result.isComplete)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `throws IllegalArgumentException when combined shortlist has fewer than 2 names`() = runTest {
        // given
        val parent1 = buildParent(id = 1L, parentIndex = 0)
        val session = buildSession(id = 1L, parent1 = parent1, parent2 = null)
        coEvery { sessionRepository.getShortlistIds(parentId = 1L) } returns listOf(1L)
        coEvery { battleRepository.saveBattleState(any()) } returns Unit

        // when
        useCase(session)

        // then - exception expected
    }

    @Test
    fun `sets targetFinalists to 4 when more than 4 names`() = runTest {
        // given
        val parent1 = buildParent(id = 1L, parentIndex = 0)
        val session = buildSession(id = 1L, parent1 = parent1, parent2 = null)
        coEvery { sessionRepository.getShortlistIds(parentId = 1L) } returns listOf(1L, 2L, 3L, 4L, 5L, 6L)
        coEvery { battleRepository.saveBattleState(any()) } returns Unit

        // when
        val result = useCase(session)

        // then
        assertEquals(4, result.targetFinalists)
    }

    @Test
    fun `sets targetFinalists equal to count when 2 names`() = runTest {
        // given
        val parent1 = buildParent(id = 1L, parentIndex = 0)
        val session = buildSession(id = 1L, parent1 = parent1, parent2 = null)
        coEvery { sessionRepository.getShortlistIds(parentId = 1L) } returns listOf(1L, 2L)
        coEvery { battleRepository.saveBattleState(any()) } returns Unit

        // when
        val result = useCase(session)

        // then
        assertEquals(2, result.targetFinalists)
    }

    // ── buildRound ────────────────────────────────────────────────────────────

    @Test
    fun `buildRound with even count produces no auto-pass duels`() {
        // given
        val ids = listOf(1L, 2L, 3L, 4L)

        // when
        val round = useCase.buildRound(1, ids)

        // then
        assertTrue(round.duels.none { it.firstName2Id == null })
        assertEquals(2, round.duels.size)
    }

    @Test
    fun `buildRound with odd count has null firstName2Id on last duel`() {
        // given
        val ids = listOf(1L, 2L, 3L)

        // when
        val round = useCase.buildRound(1, ids)

        // then
        assertEquals(2, round.duels.size)
        assertNull(round.duels.last().firstName2Id)
        assertTrue(round.duels.first().firstName2Id != null)
    }

    @Test
    fun `buildRound with 2 names produces 1 duel`() {
        // given / when
        val round = useCase.buildRound(1, listOf(1L, 2L))

        // then
        assertEquals(1, round.duels.size)
        assertEquals(1L, round.duels[0].firstName1Id)
        assertEquals(2L, round.duels[0].firstName2Id)
    }

    @Test
    fun `buildRound assigns correct round number`() {
        // given / when
        val round = useCase.buildRound(3, listOf(1L, 2L))

        // then
        assertEquals(3, round.roundNumber)
    }
}
