package com.telen.namebattle.data.repository

import com.telen.namebattle.data.local.dao.SessionDao
import com.telen.namebattle.data.local.entity.SessionEntity
import com.telen.namebattle.domain.model.BattleRound
import com.telen.namebattle.domain.model.BattleState
import com.telen.namebattle.domain.model.DuelState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class BattleRepositoryImplTest {

    private lateinit var dao: SessionDao
    private lateinit var repository: BattleRepositoryImpl

    private val json = Json { ignoreUnknownKeys = true }

    @Before
    fun setUp() {
        dao = mockk()
        repository = BattleRepositoryImpl(dao)
    }

    @Test
    fun `getBattleState returns null when session not found`() = runTest {
        // given
        coEvery { dao.getSessionById(1L) } returns null

        // when
        val result = repository.getBattleState(1L)

        // then
        assertNull(result)
    }

    @Test
    fun `getBattleState returns null when battleStateJson is null`() = runTest {
        // given
        val entity = SessionEntity(id = 1L, gender = "BOY", createdAt = 0L, battleStateJson = null)
        coEvery { dao.getSessionById(1L) } returns entity

        // when
        val result = repository.getBattleState(1L)

        // then
        assertNull(result)
    }

    @Test
    fun `getBattleState deserializes battle state from json`() = runTest {
        // given
        val state = BattleState(
            sessionId = 1L,
            initialCount = 4,
            targetFinalists = 4,
            rounds = listOf(
                BattleRound(
                    roundNumber = 1,
                    duels = listOf(
                        DuelState(firstName1Id = 1L, firstName2Id = 2L),
                        DuelState(firstName1Id = 3L, firstName2Id = 4L),
                    )
                )
            )
        )
        val stateJson = json.encodeToString(state)
        val entity = SessionEntity(id = 1L, gender = "BOY", createdAt = 0L, battleStateJson = stateJson)
        coEvery { dao.getSessionById(1L) } returns entity

        // when
        val result = repository.getBattleState(1L)

        // then
        assertNotNull(result)
        assertEquals(state, result)
    }

    @Test
    fun `saveBattleState serializes state and calls updateBattleState`() = runTest {
        // given
        val state = BattleState(
            sessionId = 1L,
            initialCount = 2,
            targetFinalists = 2,
            rounds = listOf(
                BattleRound(
                    roundNumber = 1,
                    duels = listOf(DuelState(firstName1Id = 1L, firstName2Id = 2L))
                )
            )
        )
        coEvery { dao.updateBattleState(any(), any()) } returns Unit

        // when
        repository.saveBattleState(state)

        // then
        coVerify(exactly = 1) {
            dao.updateBattleState(
                sessionId = 1L,
                json = match { it.isNotEmpty() }
            )
        }
    }

    @Test
    fun `clearBattleState calls updateBattleState with null`() = runTest {
        // given
        coEvery { dao.updateBattleState(any(), any()) } returns Unit

        // when
        repository.clearBattleState(42L)

        // then
        coVerify(exactly = 1) { dao.updateBattleState(sessionId = 42L, json = null) }
    }
}
