package com.telen.namebattle.presentation.auth

data class ParentOption(
    val parentIndex: Int,
    val name: String,
    val shortlistCount: Int,
    val listValidated: Boolean,
)

data class AuthUiState(
    val isLoading: Boolean = true,
    val parents: List<ParentOption> = emptyList(),
    val totalNames: Int = 0,
    val selectedParentIndex: Int? = null,
    val password: String = "",
    val isChecking: Boolean = false,
    val error: String? = null,
    val canStartBattle: Boolean = false,
)
