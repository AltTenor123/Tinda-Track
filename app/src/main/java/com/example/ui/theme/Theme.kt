package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = Forest,
    onPrimary = Color.White,
    primaryContainer = SageSoft,
    onPrimaryContainer = Forest,
    secondary = ForestSoft,
    onSecondary = Color.White,
    secondaryContainer = SageLight,
    onSecondaryContainer = Ink,
    tertiary = Amber,
    onTertiary = Color.White,
    tertiaryContainer = AmberLight,
    onTertiaryContainer = Amber,
    background = Cream,
    onBackground = Ink,
    surface = CardCream,
    onSurface = Ink,
    surfaceVariant = CreamDark,
    onSurfaceVariant = Muted,
    outline = CardBorder,
    outlineVariant = SageSoft,
    error = AlertRed,
    onError = Color.White,
    errorContainer = AlertRedLight,
    onErrorContainer = AlertRed
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkForestAccent,
    onPrimary = Color.White,
    primaryContainer = DarkForestSoft,
    onPrimaryContainer = DarkText,
    secondary = Sage,
    onSecondary = DarkBackground,
    secondaryContainer = DarkCard,
    onSecondaryContainer = DarkText,
    tertiary = Amber,
    onTertiary = Color.White,
    tertiaryContainer = DarkCard,
    onTertiaryContainer = Amber,
    background = DarkBackground,
    onBackground = DarkText,
    surface = DarkCard,
    onSurface = DarkText,
    surfaceVariant = DarkSurface,
    onSurfaceVariant = DarkTextMuted,
    outline = DarkCardBorder,
    outlineVariant = DarkCardBorder,
    error = AlertRed,
    onError = Color.White,
    errorContainer = DarkCard,
    onErrorContainer = AlertRed
)

val TindaShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun TindaTrackTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val extraColors = if (darkTheme) {
        TindaExtraColors(
            isDark = true,
            forest = DarkForestAccent,
            forestSoft = DarkForestSoft,
            cream = DarkBackground,
            cardCream = DarkCard,
            cardBorder = DarkCardBorder,
            sage = Sage,
            sageSoft = DarkSageSoft,
            ink = DarkText,
            muted = DarkTextMuted,
            amber = Amber,
            amberLight = DarkCard,
            alert = AlertRed,
            alertLight = DarkCard,
            success = Color(0xFF34A853),
            successLight = DarkCard,
            gold = AccentGold
        )
    } else {
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

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            @Suppress("DEPRECATION")
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    CompositionLocalProvider(
        LocalTindaColors provides extraColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = TindaTypography,
            shapes = TindaShapes,
            content = content
        )
    }
}
