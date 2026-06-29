package com.telen.namebattle.presentation.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class NbColors(
    val page: Color,
    val bg: Color,
    val bg2: Color,
    val bg3: Color,
    val border: Color,
    val border2: Color,
    val textHi: Color,
    val textMid: Color,
    val textLo: Color,
    val accent: Color,
    val accentBg: Color,
    val accentBorder: Color,
    val pro: Color,
    val proBg: Color,
    val proBorder: Color,
    val success: Color,
    val successBg: Color,
    val successBorder: Color,
    val warning: Color,
    val warningBg: Color,
    val danger: Color,
    val dangerBorder: Color,
)

val DarkNbColors = NbColors(
    page = Page, bg = Bg, bg2 = Bg2, bg3 = Bg3,
    border = Border, border2 = Border2,
    textHi = TextHi, textMid = TextMid, textLo = TextLo,
    accent = Accent, accentBg = AccentBg, accentBorder = AccentBorder,
    pro = Pro, proBg = ProBg, proBorder = ProBorder,
    success = Success, successBg = SuccessBg, successBorder = SuccessBorder,
    warning = Warning, warningBg = WarningBg,
    danger = Danger, dangerBorder = DangerBorder,
)

val LightNbColors = NbColors(
    page = PageLight, bg = BgLight, bg2 = Bg2Light, bg3 = Bg3Light,
    border = BorderLight, border2 = Border2Light,
    textHi = TextHiLight, textMid = TextMidLight, textLo = TextLoLight,
    accent = AccentLight, accentBg = AccentBgLight, accentBorder = AccentBorderLight,
    pro = ProLight, proBg = ProBgLight, proBorder = ProBorderLight,
    success = SuccessLight, successBg = SuccessBgLight, successBorder = SuccessBorderLight,
    warning = WarningLight, warningBg = WarningBgLight,
    danger = DangerLight, dangerBorder = DangerBorderLight,
)

val LocalNbColors = staticCompositionLocalOf { DarkNbColors }
