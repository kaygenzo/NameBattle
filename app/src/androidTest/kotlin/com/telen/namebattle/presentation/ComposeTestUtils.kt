package com.telen.namebattle.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import com.telen.namebattle.presentation.theme.NameBattleTheme

fun ComposeContentTestRule.setThemedContent(content: @Composable () -> Unit) {
    setContent {
        NameBattleTheme { content() }
    }
}
