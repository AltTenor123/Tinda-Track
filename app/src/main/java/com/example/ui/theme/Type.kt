package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

// Custom Font Families bundled locally
val FrauncesFontFamily = FontFamily(
    Font(R.font.fraunces, FontWeight.Normal),
    Font(R.font.fraunces, FontWeight.SemiBold),
    Font(R.font.fraunces, FontWeight.Bold)
)

val DmSansFontFamily = FontFamily(
    Font(R.font.dm_sans, FontWeight.Normal),
    Font(R.font.dm_sans, FontWeight.Medium),
    Font(R.font.dm_sans, FontWeight.Bold)
)

// Complete Material 3 Typography Scale for Tinda Track
// Note: Colors are left Unspecified so components automatically inherit LocalContentColor
val TindaTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FrauncesFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 40.sp,
        lineHeight = 48.sp,
        letterSpacing = (-0.5).sp,
        color = Color.Unspecified
    ),
    displayMedium = TextStyle(
        fontFamily = FrauncesFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 38.sp,
        letterSpacing = (-0.25).sp,
        color = Color.Unspecified
    ),
    displaySmall = TextStyle(
        fontFamily = FrauncesFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        color = Color.Unspecified
    ),
    headlineLarge = TextStyle(
        fontFamily = FrauncesFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        lineHeight = 32.sp,
        color = Color.Unspecified
    ),
    headlineMedium = TextStyle(
        fontFamily = FrauncesFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        color = Color.Unspecified
    ),
    headlineSmall = TextStyle(
        fontFamily = FrauncesFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        color = Color.Unspecified
    ),
    titleLarge = TextStyle(
        fontFamily = FrauncesFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        color = Color.Unspecified
    ),
    titleMedium = TextStyle(
        fontFamily = DmSansFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        color = Color.Unspecified
    ),
    titleSmall = TextStyle(
        fontFamily = DmSansFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        color = Color.Unspecified
    ),
    bodyLarge = TextStyle(
        fontFamily = DmSansFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        color = Color.Unspecified
    ),
    bodyMedium = TextStyle(
        fontFamily = DmSansFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        color = Color.Unspecified
    ),
    bodySmall = TextStyle(
        fontFamily = DmSansFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        color = Color.Unspecified
    ),
    labelLarge = TextStyle(
        fontFamily = DmSansFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        color = Color.Unspecified
    ),
    labelMedium = TextStyle(
        fontFamily = DmSansFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
        color = Color.Unspecified
    ),
    labelSmall = TextStyle(
        fontFamily = DmSansFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.5.sp,
        color = Color.Unspecified
    )
)
