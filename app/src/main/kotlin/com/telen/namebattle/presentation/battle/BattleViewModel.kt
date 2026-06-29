package com.telen.namebattle.presentation.battle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.telen.namebattle.domain.model.BattleState
import com.telen.namebattle.domain.usecase.battle.ChooseWinnerUseCase
import com.telen.namebattle.domain.usecase.battle.GetBattleStateUseCase
import com.telen.namebattle.domain.usecase.firstname.GetNamesByIdsUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface BattleUiEvent {
    data object Complete : BattleUiEvent
}

class BattleViewModel(
    private val sessionId: Long,
    private val getBattleState: GetBattleStateUseCase,
    private val chooseWinner: ChooseWinnerUseCase,
    private val getNamesByIds: GetNamesByIdsUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(BattleUiState())
    val state: StateFlow<BattleUiState> = _state.asStateFlow()

    private val _events = Channel<BattleUiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private var battle: BattleState? = null
    private val names = mutableMapOf<Long, String>()

    init {
        load()
    }

    private fun nameOf(id: Long) = names[id] ?: "?"

    private fun load() {
        viewModelScope.launch {
            val state = getBattleState(sessionId) ?: return@launch
            battle = state
            val allIds = state.rounds.flatMap { r ->
                r.duels.flatMap { listOfNotNull(it.firstName1Id, it.firstName2Id) }
            }.distinct()
            getNamesByIds(allIds).forEach { names[it.id] = it.name }
            renderCurrent(state)
        }
    }

    private fun renderCurrent(state: BattleState) {
        val duel = state.currentDuel ?: return
        val progress = if (state.totalDuelsInRound == 0) 0f
        else state.currentDuelIndex.toFloat() / state.totalDuelsInRound
        _state.update { s ->
            val base = s.copy(
                isLoading = false,
                roundNumber = state.currentRoundNumber,
                position = state.duelPositionLabel,
                progress = progress,
                duelKey = s.duelKey + 1,
                summary = null,
            )
            if (duel.firstName2Id == null) {
                base.copy(
                    mode = BattleMode.AUTO_PASS,
                    autoId = duel.firstName1Id,
                    autoName = nameOf(duel.firstName1Id),
                )
            } else {
                base.copy(
                    mode = BattleMode.DUEL,
                    leftId = duel.firstName1Id,
                    leftName = nameOf(duel.firstName1Id),
                    rightId = duel.firstName2Id!!,
                    rightName = nameOf(duel.firstName2Id!!),
                )
            }
        }
    }

    fun choose(winnerId: Long) {
        viewModelScope.launch {
            val old = battle ?: return@launch
            val oldRoundIndex = old.currentRoundIndex
            val updated = chooseWinner(sessionId, winnerId)
            battle = updated
            when {
                updated.isComplete -> _events.send(BattleUiEvent.Complete)
                updated.currentRoundIndex > oldRoundIndex -> showSummary(updated, oldRoundIndex)
                else -> renderCurrent(updated)
            }
        }
    }

    fun continueAfterSummary() {
        battle?.let { renderCurrent(it) }
    }

    private fun showSummary(state: BattleState, finishedRoundIndex: Int) {
        val finished = state.rounds[finishedRoundIndex]
        val survivors = finished.winners.map { nameOf(it) }
        val eliminated = finished.duels.mapNotNull { d ->
            val w = d.winnerId
            if (d.firstName2Id == null || w == null) null
            else {
                val loser = if (w == d.firstName1Id) d.firstName2Id else d.firstName1Id
                nameOf(loser)
            }
        }
        val next = state.currentRound
        val nextDuels = next?.duels?.count { it.firstName2Id != null } ?: 0
        val nextHasAuto = next?.duels?.any { it.firstName2Id == null } ?: false
        _state.update {
            it.copy(
                mode = BattleMode.ROUND_SUMMARY,
                summary = RoundSummary(
                    finishedRound = finished.roundNumber,
                    nextRound = state.currentRoundNumber,
                    survivors = survivors,
                    eliminated = eliminated,
                    nextDuels = nextDuels,
                    nextHasAuto = nextHasAuto,
                    target = state.targetFinalists,
                ),
            )
        }
    }
}
