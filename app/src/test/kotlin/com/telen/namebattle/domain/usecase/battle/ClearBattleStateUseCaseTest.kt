package com.telen.namebattle.domain.usecase.battle

import com.telen.namebattle.domain.repository.BattleRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class ClearBattleStateUseCaseTest {

    private lateinit var battleRepository: BattleRepository
    private lateinit var useCase: ClearBattleStateUseCase

    @Before
    fun setUp() {
        battleRepository = mockk()
        useCase = ClearBattleStateUseCase(battleRepository)
    }

    @Test
    fun `delegates to repo clearBattleState with the given sessionId`() = runTest {
        // given
        coEvery { battleRepository.clearBattleState(sessionId = 1L) } returns Unit

        // when
        useCase(sessionId = 1L)

        // then
        coVerify(exactly = 1) { battleRepository.clearBattleState(sessionId = 1L) }
    }
}
