package com.telen.namebattle.presentation.setup

import com.telen.namebattle.domain.model.Gender

data class SetupUiState(
    val gender: Gender = Gender.BOY,
    val soloMode: Boolean = false,

    val parent1Name: String = "",
    val parent1Password: String = "",
    val parent1Confirm: String = "",
    val parent1Locked: Boolean = false,
    val parent1PasswordsMatch: Boolean = false,
    val parent1CanLock: Boolean = false,

    val parent2Name: String = "",
    val parent2Password: String = "",
    val parent2Confirm: String = "",
    val parent2Locked: Boolean = false,
    val parent2PasswordsMatch: Boolean = false,
    val parent2CanLock: Boolean = false,

    val canCreate: Boolean = false,
    val isCreating: Boolean = false,
)
