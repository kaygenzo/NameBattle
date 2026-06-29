package com.telen.namebattle.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.telen.namebattle.presentation.theme.NbTheme

enum class PillTone { ACCENT, SUCCESS, WARNING, PRO }

@Composable
fun Pill(text: String, modifier: Modifier = Modifier, tone: PillTone = PillTone.ACCENT) {
    val c = NbTheme.colors
    val (fg, bg, border) = when (tone) {
        PillTone.ACCENT -> Triple(c.accent, c.accentBg, c.accentBorder)
        PillTone.SUCCESS -> Triple(c.success, c.successBg, c.successBorder)
        PillTone.WARNING -> Triple(c.warning, c.warningBg, Color.Transparent)
        PillTone.PRO -> Triple(c.pro, c.proBg, c.proBorder)
    }
    val shape = RoundedCornerShape(20.dp)
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = fg,
        modifier = modifier
            .background(bg, shape)
            .then(
                if (border == Color.Transparent) Modifier else Modifier.border(
                    1.dp,
                    border,
                    shape
                )
            )
            .padding(horizontal = 8.dp, vertical = 3.dp),
    )
}
