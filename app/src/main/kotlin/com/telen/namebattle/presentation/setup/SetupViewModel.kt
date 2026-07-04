package com.telen.namebattle.presentation.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.telen.namebattle.domain.model.Gender
import com.telen.namebattle.domain.usecase.session.CreateSessionUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface SetupUiEvent {
    data class SessionCreated(val sessionId: Long) : SetupUiEvent
}

class SetupViewModel(
    private val createSession: CreateSessionUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(SetupUiState())
    val state: StateFlow<SetupUiState> = _state.asStateFlow()

    private val _events = Channel<SetupUiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun onGenderChange(g: Gender) = _state.update { it.copy(gender = g) }
    fun onSoloToggle(v: Boolean) {
        _state.update { it.copy(soloMode = v).recompute() }
        if (_state.value.canCreate) createSession()
    }

    // Parent 1
    fun onParent1Name(v: String) = _state.update { it.copy(parent1Name = v).recompute() }
    fun onParent1Password(v: String) = _state.update { it.copy(parent1Password = v).recompute() }
    fun onParent1Confirm(v: String) = _state.update { it.copy(parent1Confirm = v).recompute() }
    fun lockParent1() {
        if (!_state.value.parent1CanLock) return
        _state.update { it.copy(parent1Locked = true).recompute() }
        if (_state.value.canCreate) createSession()
    }

    // Parent 2
    fun onParent2Name(v: String) = _state.update { it.copy(parent2Name = v).recompute() }
    fun onParent2Password(v: String) = _state.update { it.copy(parent2Password = v).recompute() }
    fun onParent2Confirm(v: String) = _state.update { it.copy(parent2Confirm = v).recompute() }
    fun lockParent2() {
        if (!_state.value.parent2CanLock) return
        _state.update { it.copy(parent2Locked = true).recompute() }
        if (_state.value.canCreate) createSession()
    }

    private fun SetupUiState.recompute(): SetupUiState {
        val p1Match = parent1Password.isNotEmpty() && parent1Password == parent1Confirm
        val p2Match = parent2Password.isNotEmpty() && parent2Password == parent2Confirm
        return copy(
            parent1PasswordsMatch = p1Match,
            parent1CanLock = parent1Name.isNotBlank() && p1Match,
            parent2PasswordsMatch = p2Match,
            parent2CanLock = parent2Name.isNotBlank() && p2Match,
            canCreate = parent1Locked && (soloMode || parent2Locked),
        )
    }

    private fun createSession() {
        val s = _state.value
        if (s.isCreating) return
        _state.update { it.copy(isCreating = true) }
        viewModelScope.launch {
            try {
                val session = createSession(
                    CreateSessionUseCase.Params(
                        gender = s.gender,
                        parent1Name = s.parent1Name.trim(),
                        parent1Password = s.parent1Password,
                        parent2Name = if (s.soloMode) null else s.parent2Name.trim(),
                        parent2Password = if (s.soloMode) null else s.parent2Password,
                    )
                )
                _events.send(SetupUiEvent.SessionCreated(session.id))
            } catch (e: Exception) {
                _state.update { it.copy(isCreating = false) }
            }
        }
    }
}
