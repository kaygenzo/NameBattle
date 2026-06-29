package com.telen.namebattle.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.telen.namebattle.presentation.theme.NbTheme
import com.telen.namebattle.presentation.theme.White

private val BtnShape = RoundedCornerShape(10.dp)

/** Prototype `.btn.bp` — solid accent. */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = BtnShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = NbTheme.colors.accent,
            contentColor = White,
        ),
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 46.dp),
    ) { Text(text) }
}

/** Prototype `.btn.bs` — transparent with border2. */
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shape = BtnShape,
        border = BorderStroke(1.dp, NbTheme.colors.border2),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = NbTheme.colors.textHi),
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 44.dp),
    ) { Text(text) }
}

/** Prototype `.btn.bs` tinted danger (used for "Nouvelle session"). */
@Composable
fun DangerOutlineButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick = onClick,
        shape = BtnShape,
        border = BorderStroke(1.dp, NbTheme.colors.dangerBorder),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = NbTheme.colors.danger),
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 44.dp),
    ) { Text(text) }
}
