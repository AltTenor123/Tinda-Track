package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun TindaPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    val colors = LocalTindaColors.current
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        shape = RoundedCornerShape(100.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = colors.forest,
            contentColor = Color.White,
            disabledContainerColor = colors.forestSoft.copy(alpha = 0.5f),
            disabledContentColor = Color.White.copy(alpha = 0.6f)
        ),
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 50.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = Color.White,
                strokeWidth = 2.dp
            )
        } else {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = text,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = DmSansFontFamily,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun TindaSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true
) {
    val colors = LocalTindaColors.current
    val contentColor = if (colors.isDark) Color.White else colors.forest
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(100.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = colors.sageSoft,
            contentColor = contentColor,
            disabledContainerColor = colors.cardBorder,
            disabledContentColor = colors.muted
        ),
        modifier = modifier
            .defaultMinSize(minHeight = 48.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = text,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = DmSansFontFamily,
                color = contentColor
            )
        }
    }
}

@Composable
fun TindaOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    borderColor: Color? = null
) {
    val colors = LocalTindaColors.current
    val border = borderColor ?: colors.cardBorder
    val contentColor = colors.ink
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(100.dp),
        border = BorderStroke(1.dp, border),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = contentColor
        ),
        modifier = modifier
            .defaultMinSize(minHeight = 48.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = text,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = DmSansFontFamily,
                color = contentColor
            )
        }
    }
}

@Composable
fun TindaDangerButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(100.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = AlertRed,
            contentColor = Color.White
        ),
        modifier = modifier
            .defaultMinSize(minHeight = 48.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = text,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = DmSansFontFamily,
                color = Color.White
            )
        }
    }
}
