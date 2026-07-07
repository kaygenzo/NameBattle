package com.telen.namebattle.presentation.results

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.telen.namebattle.domain.usecase.battle.GetBattleStateUseCase
import com.telen.namebattle.domain.usecase.export.ExportBattleReportUseCase
import com.telen.namebattle.domain.usecase.firstname.GetNamesByIdsUseCase
import com.telen.namebattle.domain.usecase.session.GetSessionUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

sealed interface ResultsEffect {
    data class SharePdf(val file: File) : ResultsEffect
}

class ResultsViewModel(
    private val sessionId: Long,
    private val getSession: GetSessionUseCase,
    private val getBattleState: GetBattleStateUseCase,
    private val getNamesByIds: GetNamesByIdsUseCase,
    private val exportBattleReport: ExportBattleReportUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(ResultsUiState())
    val state: StateFlow<ResultsUiState> = _state.asStateFlow()

    private val _effects = Channel<ResultsEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

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

    fun onExportPdf() {
        if (_state.value.isExporting) return
        viewModelScope.launch {
            _state.update { it.copy(isExporting = true) }
            val file = withContext(Dispatchers.IO) { exportBattleReport(sessionId) }
            _state.update { it.copy(isExporting = false) }
            if (file != null) _effects.send(ResultsEffect.SharePdf(file))
        }
    }
}
