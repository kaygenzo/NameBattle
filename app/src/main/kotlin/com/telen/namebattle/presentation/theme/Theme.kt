package com.telen.namebattle.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable

private val NbDarkScheme = darkColorScheme(
    primary = Accent,
    onPrimary = White,
    primaryContainer = AccentBg,
    secondary = Pro,
    onSecondary = White,
    background = Page,
    onBackground = TextHi,
    surface = Bg,
    onSurface = TextHi,
    surfaceVariant = Bg3,
    onSurfaceVariant = TextMid,
    error = Danger,
    onError = White,
    outline = Border2,
    outlineVariant = Border,
)

private val NbLightScheme = lightColorScheme(
    primary = AccentLight,
    onPrimary = White,
    primaryContainer = AccentBgLight,
    secondary = ProLight,
    onSecondary = White,
    background = PageLight,
    onBackground = TextHiLight,
    surface = BgLight,
    onSurface = TextHiLight,
    surfaceVariant = Bg3Light,
    onSurfaceVariant = TextMidLight,
    error = DangerLight,
    onError = White,
    outline = Border2Light,
    outlineVariant = BorderLight,
)

@Composable
fun NameBattleTheme(content: @Composable () -> Unit) {
    val isDark = isSystemInDarkTheme()
    val nbColors = if (isDark) DarkNbColors else LightNbColors
    val colorScheme = if (isDark) NbDarkScheme else NbLightScheme

    CompositionLocalProvider(LocalNbColors provides nbColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = NameBattleTypography,
            content = content,
        )
    }
}

object NbTheme {
    val colors: NbColors
        @Composable @ReadOnlyComposable
        get() = LocalNbColors.current
}
