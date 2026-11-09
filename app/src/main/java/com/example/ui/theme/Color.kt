package com.example.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Light Theme Design Tokens
val Forest = Color(0xFF0E2F24)
val ForestSoft = Color(0xFF1D4A36)
val ForestMedium = Color(0xFF285C45)
val ForestLight = Color(0xFF3B7B5E)

val Cream = Color(0xFFF7F4EC)
val CreamLight = Color(0xFFFAF7F0)
val CreamDark = Color(0xFFECE5D6)
val CardCream = Color(0xFFFFFFFF)
val CardBorder = Color(0xFFE8E2D5)

val Sage = Color(0xFF86A88B)
val SageDeep = Color(0xFF5E8565)
val SageSoft = Color(0xFFE7EFE9)
val SageLight = Color(0xFFF1F6F2)

val Ink = Color(0xFF11231A)
val InkLight = Color(0xFF2A3E33)
val Muted = Color(0xFF526356)
val MutedLight = Color(0xFF78897C)

// Alert & Status Colors (Always Color + Text)
val Amber = Color(0xFFB7791F)
val AmberLight = Color(0xFFFDF4E7)
val AmberBorder = Color(0xFFF2D6A5)

val AlertRed = Color(0xFFA64035)
val AlertRedLight = Color(0xFFFBECEB)
val AlertRedBorder = Color(0xFFF3BFBA)

val SuccessGreen = Color(0xFF1F7843)
val SuccessGreenLight = Color(0xFFE8F6EE)
val SuccessGreenBorder = Color(0xFFB7E2C8)

val AccentGold = Color(0xFFD4A338)
val AccentGoldLight = Color(0xFFFFF7E6)

// Dark Theme Variants (Deep OLED Forest & Slate)
val DarkBackground = Color(0xFF0B1612)
val DarkSurface = Color(0xFF10211B)
val DarkCard = Color(0xFF152A22)
val DarkCardBorder = Color(0xFF223E33)
val DarkText = Color(0xFFF5F3EB)
val DarkTextMuted = Color(0xFFA4B8AA)
val DarkForestAccent = Color(0xFF3EA36D)
val DarkForestSoft = Color(0xFF2B704C)
val DarkSageSoft = Color(0xFF1D3B2F)

// Pastel Landing Page Accents (Faithful to reference)
val PastelPeach = Color(0xFFFEE2D1)
val PastelPeachBorder = Color(0xFFF4C6AB)
val PastelBlue = Color(0xFFD2E3FC)
val PastelBlueBorder = Color(0xFFB0CDFA)
val PastelMint = Color(0xFFCCE6D0)
val PastelMintBorder = Color(0xFFA5D1AC)

@Immutable
data class TindaExtraColors(
    val isDark: Boolean,
    val forest: Color,
    val forestSoft: Color,
    val cream: Color,
    val cardCream: Color,
    val cardBorder: Color,
    val sage: Color,
    val sageSoft: Color,
    val ink: Color,
    val muted: Color,
    val amber: Color,
    val amberLight: Color,
    val alert: Color,
    val alertLight: Color,
    val success: Color,
    val successLight: Color,
    val gold: Color
)

val LocalTindaColors = staticCompositionLocalOf {
    TindaExtraColors(
        isDark = false,
        forest = Forest,
        forestSoft = ForestSoft,
        cream = Cream,
        cardCream = CardCream,
        cardBorder = CardBorder,
        sage = Sage,
        sageSoft = SageSoft,
        ink = Ink,
        muted = Muted,
        amber = Amber,
        amberLight = AmberLight,
        alert = AlertRed,
        alertLight = AlertRedLight,
        success = SuccessGreen,
        successLight = SuccessGreenLight,
        gold = AccentGold
    )
}
