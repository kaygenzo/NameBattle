package com.telen.namebattle.presentation.results

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.telen.namebattle.domain.usecase.battle.GetBattleStateUseCase
import com.telen.namebattle.domain.usecase.firstname.GetNamesByIdsUseCase
import com.telen.namebattle.domain.usecase.session.GetSessionUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ResultsViewModel(
    private val sessionId: Long,
    private val getSession: GetSessionUseCase,
    private val getBattleState: GetBattleStateUseCase,
    private val getNamesByIds: GetNamesByIdsUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(ResultsUiState())
    val state: StateFlow<ResultsUiState> = _state.asStateFlow()

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            val session = getSession(sessionId) ?: run {
                _state.update { it.copy(isLoading = false) }
                return@launch
            }
            val battle = getBattleState(session.id)
            if (battle == null) {
                _state.update { it.copy(isLoading = false) }
                return@launch
            }
            val names = getNamesByIds(battle.finalists).map { it.name }
            _state.update {
                it.copy(
                    isLoading = false,
                    finalists = names,
                    roundsPlayed = battle.rounds.size,
                )
            }
        }
    }
}
