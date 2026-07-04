package com.telen.namebattle.domain.usecase.battle

import com.telen.namebattle.domain.repository.BattleRepository
import com.telen.namebattle.util.buildBattleState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class GetBattleStateUseCaseTest {

    private lateinit var battleRepository: BattleRepository
    private lateinit var useCase: GetBattleStateUseCase

    @Before
    fun setUp() {
        battleRepository = mockk()
        useCase = GetBattleStateUseCase(battleRepository)
    }

    @Test
    fun `delegates to battleRepository getBattleState and returns result`() = runTest {
        // given
        val expectedState = buildBattleState(sessionId = 1L)
        coEvery { battleRepository.getBattleState(sessionId = 1L) } returns expectedState

        // when
        val result = useCase(sessionId = 1L)

        // then
        assertEquals(expectedState, result)
        coVerify(exactly = 1) { battleRepository.getBattleState(sessionId = 1L) }
    }

    @Test
    fun `returns null when battle state does not exist`() = runTest {
        // given
        coEvery { battleRepository.getBattleState(sessionId = 99L) } returns null

        // when
        val result = useCase(sessionId = 99L)

        // then
        assertNull(result)
    }
}
