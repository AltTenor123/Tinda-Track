package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.LocalTindaColors

@Composable
fun TindaCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    backgroundColor: Color? = null,
    borderColor: Color? = null,
    borderWidth: Dp = 1.dp,
    elevation: Dp = 2.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val colors = LocalTindaColors.current
    val bg = backgroundColor ?: colors.cardCream
    val border = borderColor ?: colors.cardBorder

    val cardModifier = if (onClick != null) {
        modifier.clickable(onClick = onClick)
    } else {
        modifier
    }

    Card(
        modifier = cardModifier.shadow(elevation, shape, clip = false),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = bg),
        border = BorderStroke(borderWidth, border)
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}
