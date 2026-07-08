package com.telen.namebattle.presentation.search

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.telen.namebattle.domain.model.FirstName
import com.telen.namebattle.domain.model.Gender
import com.telen.namebattle.presentation.theme.NameBattleTheme

private val sampleNames = listOf(
    NameRow(1L, "Élodie", inList = false, hasMeaning = true),
    NameRow(2L, "Emma", inList = true, hasMeaning = true),
    NameRow(3L, "Elsa", inList = false, hasMeaning = false),
    NameRow(4L, "Elise", inList = false, hasMeaning = true),
    NameRow(5L, "Eva", inList = false, hasMeaning = false),
)

private val sampleShortlist = listOf(
    NameRow(2L, "Emma", inList = true, hasMeaning = true),
    NameRow(6L, "Alice", inList = true, hasMeaning = true),
    NameRow(7L, "Léa", inList = true, hasMeaning = false),
)

@Preview(
    name = "Search – A→Z – Dark",
    showBackground = true,
    backgroundColor = 0xFF0F0F11,
    showSystemUi = true,
)
@Composable
private fun SearchAZDark() {
    NameBattleTheme {
        SearchScreenContent(
            state = SearchUiState(
                isLoading = false,
                parentName = "Sofia",
                tab = SearchTab.AZ,
                pane = SearchPane.SEARCH,
                selectedLetter = 'E',
                results = sampleNames,
                shortlistCount = 3,
            )
        )
    }
}

@Preview(
    name = "Search – A→Z – Light",
    showBackground = true,
    backgroundColor = 0xFFF5F5FA,
    showSystemUi = true,
)
@Composable
private fun SearchAZLight() {
    NameBattleTheme {
        SearchScreenContent(
            state = SearchUiState(
                isLoading = false,
                parentName = "Sofia",
                tab = SearchTab.AZ,
                pane = SearchPane.SEARCH,
                selectedLetter = 'E',
                results = sampleNames,
                shortlistCount = 3,
            )
        )
    }
}

@Preview(
    name = "Search – Top 100 – Dark",
    showBackground = true,
    backgroundColor = 0xFF0F0F11,
    showSystemUi = true,
)
@Composable
private fun SearchTop100Dark() {
    NameBattleTheme {
        SearchScreenContent(
            state = SearchUiState(
                isLoading = false,
                parentName = "Sofia",
                tab = SearchTab.TOP,
                pane = SearchPane.SEARCH,
                topYear = 1900,
                results = sampleNames,
                shortlistCount = 3,
            )
        )
    }
}

@Preview(
    name = "Search – Libre – Dark",
    showBackground = true,
    backgroundColor = 0xFF0F0F11,
    showSystemUi = true,
)
@Composable
private fun SearchFreeDark() {
    NameBattleTheme {
        SearchScreenContent(
            state = SearchUiState(
                isLoading = false,
                parentName = "Sofia",
                tab = SearchTab.FREE,
                pane = SearchPane.SEARCH,
                query = "El",
                results = sampleNames.take(3),
                shortlistCount = 3,
            )
        )
    }
}

@Preview(
    name = "Search – Ma liste – Dark",
    showBackground = true,
    backgroundColor = 0xFF0F0F11,
    showSystemUi = true,
)
@Composable
private fun SearchMyListDark() {
    NameBattleTheme {
        SearchScreenContent(
            state = SearchUiState(
                isLoading = false,
                parentName = "Sofia",
                pane = SearchPane.MY_LIST,
                shortlist = sampleShortlist,
                shortlistCount = 3,
            )
        )
    }
}

@Preview(
    name = "Search – Ma liste vide – Dark",
    showBackground = true,
    backgroundColor = 0xFF0F0F11,
    showSystemUi = true,
)
@Composable
private fun SearchMyListEmptyDark() {
    NameBattleTheme {
        SearchScreenContent(
            state = SearchUiState(
                isLoading = false,
                parentName = "Sofia",
                pane = SearchPane.MY_LIST,
                shortlist = emptyList(),
                shortlistCount = 0,
            )
        )
    }
}

@Preview(name = "NameDetailSheet – Dark", showBackground = true, backgroundColor = 0xFF222228)
@Composable
private fun NameDetailSheetDark() {
    NameBattleTheme {
        NameDetailContent(
            name = FirstName(
                id = 1L,
                name = "Élodie",
                gender = Gender.GIRL,
                birthsSince1900 = 62_000,
                birthsSince1980 = 45_000,
                birthsSince2000 = 8_000,
                birthsSince2010 = 2_000,
                totalBirths = 62_000,
                peakYear = 1988,
                firstYear = 1950,
                origin = "Grec",
                meaning = "Du grec « helios » (soleil) et « doron » (don)" +
                    " — « don du soleil ».",
                hasMeaning = true,
            ),
            onAdd = {}
        )
    }
}
