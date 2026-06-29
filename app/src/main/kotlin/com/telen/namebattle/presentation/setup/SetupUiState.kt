package com.telen.namebattle.presentation.setup

import com.telen.namebattle.domain.model.Gender

data class SetupUiState(
    val gender: Gender = Gender.BOY,
    val soloMode: Boolean = false,

    val parent1Name: String = "",
    val parent1Password: String = "",
    val parent1Confirm: String = "",
    val parent1Locked: Boolean = false,

    val parent2Name: String = "",
    val parent2Password: String = "",
    val parent2Confirm: String = "",
    val parent2Locked: Boolean = false,

    val isCreating: Boolean = false,
) {
    val parent1PasswordsMatch: Boolean
        get() = parent1Password.isNotEmpty() && parent1Password == parent1Confirm

    val parent1CanLock: Boolean
        get() = parent1Name.isNotBlank() && parent1PasswordsMatch

    val parent2PasswordsMatch: Boolean
        get() = parent2Password.isNotEmpty() && parent2Password == parent2Confirm

    val parent2CanLock: Boolean
        get() = parent2Name.isNotBlank() && parent2PasswordsMatch

    val canCreate: Boolean
        get() = parent1Locked && (soloMode || parent2Locked)
}
