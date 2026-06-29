package com.telen.namebattle.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.telen.namebattle.presentation.theme.NbTheme

@Composable
fun NbCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = NbTheme.colors.bg3,
        border = BorderStroke(1.dp, NbTheme.colors.border),
        modifier = modifier,
    ) {
        Column(Modifier.padding(14.dp), content = content)
    }
}
