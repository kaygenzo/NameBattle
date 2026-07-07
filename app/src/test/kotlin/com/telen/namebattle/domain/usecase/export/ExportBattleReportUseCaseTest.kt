package com.telen.namebattle.domain.usecase.export

import com.telen.namebattle.domain.model.BattleRound
import com.telen.namebattle.domain.model.BattleState
import com.telen.namebattle.domain.model.DuelState
import com.telen.namebattle.domain.model.FirstName
import com.telen.namebattle.domain.model.Gender
import com.telen.namebattle.domain.model.Parent
import com.telen.namebattle.domain.model.Session
import com.telen.namebattle.domain.repository.FirstNameRepository
import com.telen.namebattle.domain.usecase.battle.GetBattleStateUseCase
import com.telen.namebattle.domain.usecase.session.GetSessionUseCase
import com.telen.namebattle.export.BattleReportPdfGenerator
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.io.File

class ExportBattleReportUseCaseTest {

    private val getSession = mockk<GetSessionUseCase>()
    private val getBattleState = mockk<GetBattleStateUseCase>()
    private val firstNameRepository = mockk<FirstNameRepository>()
    private val pdfGenerator = mockk<BattleReportPdfGenerator>()

    private lateinit var useCase: ExportBattleReportUseCase

    private val sessionId = 1L

    @Before
    fun setUp() {
        useCase =
            ExportBattleReportUseCase(getSession, getBattleState, firstNameRepository, pdfGenerator)
    }

    private fun parent() =
        Parent(id = 1L, sessionId = sessionId, name = "Alice", passwordHash = "h", parentIndex = 0)

    private fun session() =
        Session(id = sessionId, gender = Gender.BOY, parent1 = parent(), parent2 = null)

    private fun firstName(id: Long, name: String) = FirstName(
        id = id, name = name, gender = Gender.BOY,
        birthsSince1900 = 0, birthsSince1980 = 0, birthsSince2000 = 0, birthsSince2010 = 0
    )

    private fun battleState() = BattleState(
        sessionId = sessionId,
        initialCount = 2,
        targetFinalists = 2,
        rounds = listOf(
            BattleRound(
                roundNumber = 1,
                duels = listOf(DuelState(firstName1Id = 10L, firstName2Id = 20L, winnerId = 10L))
            )
        ),
        finalists = listOf(10L),
        isComplete = true,
    )

    // ── returns null on missing session ──────────────────────────────────────

    @Test
    fun `returns null when session not found`() = runTest {
        coEvery { getSession(sessionId) } returns null

        assertNull(useCase(sessionId))
        coVerify(exactly = 0) { pdfGenerator.generate(any(), any(), any()) }
    }

    // ── returns null on missing battle state ──────────────────────────────────

    @Test
    fun `returns null when battle state not found`() = runTest {
        coEvery { getSession(sessionId) } returns session()
        coEvery { getBattleState(sessionId) } returns null

        assertNull(useCase(sessionId))
        coVerify(exactly = 0) { pdfGenerator.generate(any(), any(), any()) }
    }

    // ── calls generator with resolved names ───────────────────────────────────

    @Test
    fun `calls generator with session, battle and resolved name map`() = runTest {
        val session = session()
        val battle = battleState()
        val fakeFile = mockk<File>()

        coEvery { getSession(sessionId) } returns session
        coEvery { getBattleState(sessionId) } returns battle
        coEvery { firstNameRepository.getByIds(listOf(10L, 20L)) } returns
                listOf(firstName(10L, "Emma"), firstName(20L, "Liam"))
        coEvery {
            pdfGenerator.generate(
                session,
                battle,
                mapOf(10L to "Emma", 20L to "Liam")
            )
        } returns fakeFile

        val result = useCase(sessionId)

        assertNotNull(result)
        coVerify(exactly = 1) {
            pdfGenerator.generate(
                session,
                battle,
                mapOf(10L to "Emma", 20L to "Liam")
            )
        }
    }

    // ── deduplicates ids across rounds ────────────────────────────────────────

    @Test
    fun `deduplicates name ids across rounds before querying repository`() = runTest {
        val battle = BattleState(
            sessionId = sessionId,
            initialCount = 2,
            targetFinalists = 1,
            rounds = listOf(
                BattleRound(1, listOf(DuelState(10L, 20L, 10L))),
                BattleRound(2, listOf(DuelState(10L, null, 10L))),
            ),
            finalists = listOf(10L),
            isComplete = true,
        )
        coEvery { getSession(sessionId) } returns session()
        coEvery { getBattleState(sessionId) } returns battle
        coEvery { firstNameRepository.getByIds(listOf(10L, 20L)) } returns
                listOf(firstName(10L, "Emma"), firstName(20L, "Liam"))
        coEvery { pdfGenerator.generate(any(), any(), any()) } returns mockk()

        useCase(sessionId)

        coVerify(exactly = 1) { firstNameRepository.getByIds(listOf(10L, 20L)) }
    }
}
