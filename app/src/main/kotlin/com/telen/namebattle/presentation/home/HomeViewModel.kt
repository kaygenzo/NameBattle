package com.telen.namebattle.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.telen.namebattle.data.local.DatabaseSeeder
import com.telen.namebattle.domain.usecase.battle.ClearBattleStateUseCase
import com.telen.namebattle.domain.usecase.battle.GetBattleStateUseCase
import com.telen.namebattle.domain.usecase.export.ExportBattleReportUseCase
import com.telen.namebattle.domain.usecase.firstname.GetShortlistIdsUseCase
import com.telen.namebattle.domain.usecase.session.DeleteSessionUseCase
import com.telen.namebattle.domain.usecase.session.GetAllSessionsUseCase
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

sealed interface HomeUiEvent {
    data object NavigateToSetup : HomeUiEvent
    data class SharePdf(val file: File) : HomeUiEvent
}

class HomeViewModel(
    private val getAllSessions: GetAllSessionsUseCase,
    private val deleteSession: DeleteSessionUseCase,
    private val getShortlistIds: GetShortlistIdsUseCase,
    private val getBattleState: GetBattleStateUseCase,
    private val clearBattleState: ClearBattleStateUseCase,
    private val exportBattleReport: ExportBattleReportUseCase,
    private val seeder: DatabaseSeeder,
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    private val _events = Channel<HomeUiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            seeder.seedIfNeeded()
            refresh()
        }
    }

    fun refresh() {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            val sessions = getAllSessions()
            val summaries = sessions.map { session ->
                val parents = listOfNotNull(session.parent1, session.parent2)
                val counts = parents.map { getShortlistIds(it.id).size }
                val battleState = getBattleState(session.id)
                val battleStatus = when {
                    battleState == null -> BattleStatus.NOT_STARTED
                    battleState.isComplete -> BattleStatus.COMPLETED
                    else -> BattleStatus.IN_PROGRESS
                }
                SessionSummary(
                    sessionId = session.id,
                    parentNames = parents.joinToString(" & ") { it.name },
                    gender = session.gender,
                    totalNames = counts.sum(),
                    allListsValidated = parents.all { it.listValidated },
                    canStartBattle = counts.all { it > 0 },
                    battleStatus = battleStatus,
                )
            }
            _state.update { it.copy(isLoading = false, sessions = summaries) }
        }
    }

    fun onCreateSession() {
        viewModelScope.launch { _events.send(HomeUiEvent.NavigateToSetup) }
    }

    fun onDeleteSession(sessionId: Long) {
        _state.update { it.copy(pendingDeleteSessionId = sessionId) }
    }

    fun onDeleteConfirmed() {
        val sessionId = _state.value.pendingDeleteSessionId ?: return
        viewModelScope.launch {
            deleteSession(sessionId)
            _state.update { s ->
                s.copy(
                    pendingDeleteSessionId = null,
                    sessions = s.sessions.filter { it.sessionId != sessionId }
                )
            }
        }
    }

    fun onDeleteDismissed() = _state.update { it.copy(pendingDeleteSessionId = null) }

    fun onRestartBattle(sessionId: Long, onDone: () -> Unit) {
        viewModelScope.launch {
            clearBattleState(sessionId)
            _state.update { s ->
                s.copy(sessions = s.sessions.map {
                    if (it.sessionId == sessionId) it.copy(battleStatus = BattleStatus.NOT_STARTED)
                    else it
                })
            }
            onDone()
        }
    }

    fun onExportPdf(sessionId: Long) {
        if (_state.value.isExportingSessionId != null) return
        viewModelScope.launch {
            _state.update { it.copy(isExportingSessionId = sessionId) }
            val file = withContext(Dispatchers.IO) { exportBattleReport(sessionId) }
            _state.update { it.copy(isExportingSessionId = null) }
            if (file != null) _events.send(HomeUiEvent.SharePdf(file))
        }
    }
}
