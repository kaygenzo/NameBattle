package com.telen.namebattle.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.Male
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.telen.namebattle.domain.model.Gender
import com.telen.namebattle.presentation.theme.NbTheme

@Composable
fun GenderIcon(gender: Gender, modifier: Modifier = Modifier) {
    val c = NbTheme.colors
    val (icon, tint) = when (gender) {
        Gender.BOY -> Icons.Default.Male to c.accent
        Gender.GIRL -> Icons.Default.Female to c.pro
    }
    Icon(imageVector = icon, contentDescription = gender.name, tint = tint, modifier = modifier)
}
