package com.telen.namebattle.domain.usecase.export

import com.telen.namebattle.domain.repository.FirstNameRepository
import com.telen.namebattle.domain.usecase.battle.GetBattleStateUseCase
import com.telen.namebattle.domain.usecase.session.GetSessionUseCase
import com.telen.namebattle.export.BattleReportPdfGenerator
import java.io.File

class ExportBattleReportUseCase(
    private val getSession: GetSessionUseCase,
    private val getBattleState: GetBattleStateUseCase,
    private val firstNameRepository: FirstNameRepository,
    private val pdfGenerator: BattleReportPdfGenerator,
) {
    suspend operator fun invoke(sessionId: Long): File? {
        val session = getSession(sessionId) ?: return null
        val battle = getBattleState(session.id) ?: return null

        val allIds = battle.rounds
            .flatMap { round ->
                round.duels.flatMap {
                    listOfNotNull(
                        it.firstName1Id,
                        it.firstName2Id
                    )
                }
            }
            .distinct()
        val namesById = firstNameRepository.getByIds(allIds).associate { it.id to it.name }

        return pdfGenerator.generate(session, battle, namesById)
    }
}
