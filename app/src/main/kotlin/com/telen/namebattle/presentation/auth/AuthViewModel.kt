package com.telen.namebattle.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.telen.namebattle.domain.usecase.auth.AuthenticateParentUseCase
import com.telen.namebattle.domain.usecase.firstname.GetShortlistIdsUseCase
import com.telen.namebattle.domain.usecase.session.GetParentUseCase
import com.telen.namebattle.domain.usecase.session.GetSessionUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface AuthUiEvent {
    data class Authenticated(val parentIndex: Int) : AuthUiEvent
    data object LaunchBattle : AuthUiEvent
}

class AuthViewModel(
    private val sessionId: Long,
    private val getSession: GetSessionUseCase,
    private val getParent: GetParentUseCase,
    private val getShortlistIds: GetShortlistIdsUseCase,
    private val authenticate: AuthenticateParentUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    private val _events = Channel<AuthUiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            val session = getSession(sessionId)
            if (session == null) {
                _state.update { it.copy(isLoading = false) }
                return@launch
            }
            val parents = listOfNotNull(session.parent1, session.parent2)
            val options = parents.mapIndexed { index, parent ->
                val count = getShortlistIds(parent.id).size
                ParentOption(
                    parentIndex = index,
                    name = parent.name,
                    shortlistCount = count,
                    listValidated = parent.listValidated,
                )
            }
            val total = options.sumOf { it.shortlistCount }
            _state.update { it.copy(isLoading = false, parents = options, totalNames = total) }
        }
    }

    fun resetSelection() {
        _state.update { it.copy(selectedParentIndex = null, password = "", error = null, isChecking = false) }
        load()
    }

    fun onSelectParent(parentIndex: Int) {
        _state.update { it.copy(selectedParentIndex = parentIndex, password = "", error = null) }
    }

    fun onPasswordChange(v: String) = _state.update { it.copy(password = v, error = null) }

    fun submit() {
        val s = _state.value
        val idx = s.selectedParentIndex ?: return
        if (s.password.isBlank() || s.isChecking) return
        _state.update { it.copy(isChecking = true, error = null) }
        viewModelScope.launch {
            val ok = authenticate(sessionId, idx, s.password)
            if (ok) {
                _events.send(AuthUiEvent.Authenticated(idx))
            } else {
                _state.update { it.copy(isChecking = false, error = "Mot de passe incorrect") }
            }
        }
    }

    fun onLaunchBattle() {
        viewModelScope.launch { _events.send(AuthUiEvent.LaunchBattle) }
    }
}
