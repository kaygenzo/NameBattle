package com.telen.namebattle.presentation.launch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.telen.namebattle.domain.usecase.battle.StartBattleUseCase
import com.telen.namebattle.domain.usecase.firstname.GetShortlistIdsUseCase
import com.telen.namebattle.domain.usecase.session.GetSessionUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.ceil
import kotlin.math.log2
import kotlin.math.max

sealed interface LaunchUiEvent {
    data object BattleStarted : LaunchUiEvent
}

class LaunchViewModel(
    private val sessionId: Long,
    private val getSession: GetSessionUseCase,
    private val getShortlistIds: GetShortlistIdsUseCase,
    private val startBattle: StartBattleUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(LaunchUiState())
    val state: StateFlow<LaunchUiState> = _state.asStateFlow()

    private val _events = Channel<LaunchUiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            val session = getSession(sessionId) ?: run {
                _state.update { it.copy(isLoading = false) }
                return@launch
            }
            val c1 = getShortlistIds(session.parent1.id).size
            val c2 = session.parent2?.let { getShortlistIds(it.id).size } ?: 0
            val total = c1 + c2
            val target = 4
            val rounds = if (total > target) ceil(log2(total.toDouble() / target)).toInt() + 1 else 1
            _state.update {
                it.copy(
                    isLoading = false,
                    parent1Name = session.parent1.name,
                    parent1Count = c1,
                    parent2Name = session.parent2?.name,
                    parent2Count = c2,
                    total = total,
                    targetFinalists = minOf(target, max(total, 1)),
                    roundsEstimate = rounds,
                    canStart = c1 > 0 && (session.parent2 == null || c2 > 0),
                )
            }
        }
    }

    fun start() {
        if (_state.value.isStarting) return
        _state.update { it.copy(isStarting = true) }
        viewModelScope.launch {
            val session = getSession(sessionId)
            if (session != null) {
                startBattle(session)
                _events.send(LaunchUiEvent.BattleStarted)
            } else {
                _state.update { it.copy(isStarting = false) }
            }
        }
    }
}
