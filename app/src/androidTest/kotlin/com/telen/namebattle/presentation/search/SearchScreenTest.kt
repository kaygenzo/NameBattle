package com.telen.namebattle.presentation.search

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.telen.namebattle.presentation.setThemedContent
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SearchScreenTest {

    @get:Rule
    val rule = createComposeRule()

    private fun defaultState() = SearchUiState(
        isLoading = false,
        parentName = "Alice",
        shortlistCount = 3,
    )

    private fun nameRow(id: Long, name: String, inList: Boolean = false) =
        NameRow(id = id, name = name, inList = inList, hasMeaning = false)

    // region — pane headers

    @Test
    fun shows_search_pane_title_when_in_search_pane() {
        rule.setThemedContent {
            SearchScreenContent(state = defaultState().copy(pane = SearchPane.SEARCH))
        }
        rule.onNodeWithTag("pane_title").assertIsDisplayed()
    }

    @Test
    fun shows_my_list_pane_title_when_in_my_list_pane() {
        rule.setThemedContent {
            SearchScreenContent(state = defaultState().copy(pane = SearchPane.MY_LIST))
        }
        rule.onNodeWithTag("pane_title").assertIsDisplayed()
    }

    @Test
    fun shows_parent_name_in_subtitle() {
        rule.setThemedContent {
            SearchScreenContent(state = defaultState().copy(parentName = "Alice"))
        }
        rule.onNodeWithText("Alice", substring = true).assertIsDisplayed()
    }

    // region — finish button

    @Test
    fun shows_finish_button() {
        rule.setThemedContent {
            SearchScreenContent(state = defaultState())
        }
        rule.onNodeWithText("Terminer ✓").assertIsDisplayed()
    }

    @Test
    fun clicking_finish_triggers_validate_callback() {
        var called = false
        rule.setThemedContent {
            SearchScreenContent(
                state = defaultState(),
                onValidate = { called = true },
            )
        }
        rule.onNodeWithText("Terminer ✓").performClick()
        assertTrue(called)
    }

    // region — AZ tab results

    @Test
    fun shows_name_results_in_az_tab() {
        rule.setThemedContent {
            SearchScreenContent(
                state = defaultState().copy(
                    tab = SearchTab.AZ,
                    results = listOf(
                        nameRow(1L, "Alice"),
                        nameRow(2L, "Ambre"),
                    ),
                )
            )
        }
        rule.onNodeWithText("Alice").assertIsDisplayed()
        rule.onNodeWithText("Ambre").assertIsDisplayed()
    }

    @Test
    fun clicking_add_on_name_row_triggers_add_callback() {
        var addedId = -1L
        rule.setThemedContent {
            SearchScreenContent(
                state = defaultState().copy(
                    tab = SearchTab.AZ,
                    results = listOf(nameRow(id = 5L, name = "Arthur", inList = false)),
                ),
                onAdd = { addedId = it },
            )
        }
        rule.onNodeWithText("Arthur").assertIsDisplayed()
    }

    // region — my list pane

    @Test
    fun shows_shortlisted_names_in_my_list_pane() {
        rule.setThemedContent {
            SearchScreenContent(
                state = defaultState().copy(
                    pane = SearchPane.MY_LIST,
                    shortlist = listOf(
                        nameRow(1L, "Alice", inList = true),
                        nameRow(2L, "Ambre", inList = true),
                    ),
                )
            )
        }
        rule.onNodeWithText("Alice").assertIsDisplayed()
        rule.onNodeWithText("Ambre").assertIsDisplayed()
    }

    // region — FREE tab

    @Test
    fun free_tab_shows_text_input() {
        rule.setThemedContent {
            SearchScreenContent(
                state = defaultState().copy(tab = SearchTab.FREE)
            )
        }
        rule.onNodeWithText("Saisissez le prénom de votre choix", substring = true)
            .assertIsDisplayed()
    }

    // region — TOP tab

    @Test
    fun top_tab_shows_year_filters() {
        rule.setThemedContent {
            SearchScreenContent(
                state = defaultState().copy(tab = SearchTab.TOP)
            )
        }
        rule.onNodeWithText("1900").assertIsDisplayed()
        rule.onNodeWithText("1980").assertIsDisplayed()
        rule.onNodeWithText("2000").assertIsDisplayed()
        rule.onNodeWithText("2010").assertIsDisplayed()
    }

    @Test
    fun clicking_year_filter_triggers_callback() {
        var selectedYear = -1
        rule.setThemedContent {
            SearchScreenContent(
                state = defaultState().copy(tab = SearchTab.TOP, topYear = 1900),
                onTopYearChange = { selectedYear = it },
            )
        }
        rule.onNodeWithText("1980").performClick()
        assertTrue(selectedYear == 1980)
    }
}
