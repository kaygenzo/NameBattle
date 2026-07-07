package com.telen.namebattle.presentation.results

data class ResultsUiState(
    val isLoading: Boolean = true,
    val finalists: List<String> = emptyList(),
    val roundsPlayed: Int = 0,
    val isExporting: Boolean = false,
)
