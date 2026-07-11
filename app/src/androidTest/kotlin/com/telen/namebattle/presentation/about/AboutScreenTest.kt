package com.telen.namebattle.presentation.about

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.telen.namebattle.presentation.setThemedContent
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AboutScreenTest {

    @get:Rule
    val rule = createComposeRule()

    // region — structure

    @Test
    fun title_is_displayed() {
        rule.setThemedContent { AboutScreen(onBack = {}) }
        rule.onNodeWithText("À propos").assertIsDisplayed()
    }

    @Test
    fun back_button_is_displayed() {
        rule.setThemedContent { AboutScreen(onBack = {}) }
        rule.onNodeWithContentDescription("Retour").assertIsDisplayed()
    }

    @Test
    fun app_version_is_displayed() {
        rule.setThemedContent { AboutScreen(onBack = {}) }
        rule.onNodeWithText("Version", substring = true).assertIsDisplayed()
    }

    // region — data sources

    @Test
    fun data_sources_section_is_displayed() {
        rule.setThemedContent { AboutScreen(onBack = {}) }
        rule.onNodeWithText("Sources de données").assertIsDisplayed()
    }

    @Test
    fun france_country_entry_is_displayed() {
        rule.setThemedContent { AboutScreen(onBack = {}) }
        rule.onNodeWithText("France").assertIsDisplayed()
    }

    @Test
    fun insee_source_is_displayed() {
        rule.setThemedContent { AboutScreen(onBack = {}) }
        rule.onNodeWithText("INSEE", substring = true).assertIsDisplayed()
    }

    // region — licenses

    @Test
    fun open_source_licenses_section_is_displayed() {
        rule.setThemedContent { AboutScreen(onBack = {}) }
        rule.onNodeWithText("Licences open source").assertIsDisplayed()
    }

    // region — navigation

    @Test
    fun clicking_back_triggers_callback() {
        var called = false
        rule.setThemedContent { AboutScreen(onBack = { called = true }) }
        rule.onNodeWithContentDescription("Retour").performClick()
        assertTrue(called)
    }
}
