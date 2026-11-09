package com.example.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.example.ui.theme.FrauncesFontFamily
import com.example.ui.theme.LocalTindaColors
import com.example.ui.theme.PlusJakartaSansFontFamily

@Composable
fun CurrencyText(
    centavos: Long,
    modifier: Modifier = Modifier,
    color: Color = LocalTindaColors.current.ink,
    fontSize: TextUnit = 20.sp,
    fontWeight: FontWeight = FontWeight.Bold,
    useSerif: Boolean = true
) {
    val pesos = centavos / 100
    val remainder = kotlin.math.abs(centavos % 100)
    val pesosFormatted = "%,d".format(pesos)
    val centavosFormatted = "%02d".format(remainder)

    val targetFontFamily = if (useSerif) FrauncesFontFamily else PlusJakartaSansFontFamily

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Bottom
    ) {
        Text(
            text = "₱",
            color = color,
            fontSize = fontSize * 0.82f,
            fontWeight = fontWeight,
            fontFamily = targetFontFamily
        )
        Text(
            text = pesosFormatted,
            color = color,
            fontSize = fontSize,
            fontWeight = fontWeight,
            fontFamily = targetFontFamily
        )
        Text(
            text = ".$centavosFormatted",
            color = color.copy(alpha = 0.85f),
            fontSize = fontSize * 0.72f,
            fontWeight = fontWeight,
            fontFamily = targetFontFamily
        )
    }
}
