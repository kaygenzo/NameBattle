package com.telen.namebattle.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.telen.namebattle.presentation.theme.NbTheme

@Composable
fun NbDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(
        thickness = 1.dp,
        color = NbTheme.colors.border,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp),
    )
}
