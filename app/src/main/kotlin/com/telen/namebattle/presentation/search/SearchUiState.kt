package com.telen.namebattle.presentation.search

import com.telen.namebattle.domain.model.FirstName

data class NameRow(
    val id: Long,
    val name: String,
    val inList: Boolean,
    val hasMeaning: Boolean,
)

enum class SearchTab { AZ, TOP, FREE }
enum class SearchPane { SEARCH, MY_LIST }

data class SearchUiState(
    val isLoading: Boolean = true,
    val parentName: String = "",
    val hasParent2: Boolean = false,
    val pane: SearchPane = SearchPane.SEARCH,
    val tab: SearchTab = SearchTab.AZ,
    val query: String = "",
    val selectedLetter: Char = 'A',
    val topYear: Int = 1900,
    val results: List<NameRow> = emptyList(),
    val shortlist: List<NameRow> = emptyList(),
    val shortlistCount: Int = 0,
    val detail: FirstName? = null,
    val isValidating: Boolean = false,
)
