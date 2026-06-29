package com.telen.namebattle.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.telen.namebattle.presentation.theme.NbTheme

/** Tinted count tile used on the Launch screen (one per parent). */
@Composable
fun StatTile(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    accent: Color,
    bg: Color,
    border: Color,
    sub: String? = null
) {
    val c = NbTheme.colors
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bg,
        border = BorderStroke(1.dp, border),
        modifier = modifier,
    ) {
        Column(
            Modifier.padding(13.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(label, style = MaterialTheme.typography.bodySmall, color = c.textMid)
            Text(value, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = accent)
            if (sub != null) Text(
                sub,
                style = MaterialTheme.typography.bodySmall,
                color = c.textMid
            )
        }
    }
}
