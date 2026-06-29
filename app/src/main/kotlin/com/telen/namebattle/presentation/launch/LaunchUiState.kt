package com.telen.namebattle.presentation.launch

data class LaunchUiState(
    val isLoading: Boolean = true,
    val parent1Name: String = "",
    val parent1Count: Int = 0,
    val parent2Name: String? = null,
    val parent2Count: Int = 0,
    val total: Int = 0,
    val targetFinalists: Int = 0,
    val roundsEstimate: Int = 0,
    val isStarting: Boolean = false,
) {
    val canStart: Boolean
        get() = parent1Count > 0 && (parent2Name == null || parent2Count > 0)
}
