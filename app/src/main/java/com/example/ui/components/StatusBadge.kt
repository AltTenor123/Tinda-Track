package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.StockStatus
import com.example.domain.model.UtangStatus
import com.example.domain.usecase.ExpiryUrgency
import com.example.ui.theme.*

@Composable
fun StatusBadge(
    text: String,
    icon: ImageVector,
    textColor: Color,
    backgroundColor: Color,
    borderColor: Color? = null,
    modifier: Modifier = Modifier
) {
    val borderModifier = if (borderColor != null) {
        Modifier.border(1.dp, borderColor, RoundedCornerShape(100.dp))
    } else Modifier

    Row(
        modifier = modifier
            .background(backgroundColor, RoundedCornerShape(100.dp))
            .then(borderModifier)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = textColor,
            modifier = Modifier.size(13.dp)
        )
        Text(
            text = text,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun StockStatusBadge(status: StockStatus, modifier: Modifier = Modifier) {
    val colors = LocalTindaColors.current
    val isDark = colors.isDark

    when (status) {
        StockStatus.IN_STOCK -> StatusBadge(
            text = "In Stock",
            icon = Icons.Default.CheckCircle,
            textColor = if (isDark) Color(0xFF5CD88E) else SuccessGreen,
            backgroundColor = if (isDark) Color(0xFF143825) else SuccessGreenLight,
            borderColor = if (isDark) Color(0xFF235E3D) else SuccessGreenBorder,
            modifier = modifier
        )
        StockStatus.LOW_STOCK -> StatusBadge(
            text = "Low Stock",
            icon = Icons.Default.Warning,
            textColor = if (isDark) Color(0xFFFFB84D) else Amber,
            backgroundColor = if (isDark) Color(0xFF38290D) else AmberLight,
            borderColor = if (isDark) Color(0xFF6B4E1A) else AmberBorder,
            modifier = modifier
        )
        StockStatus.OUT_OF_STOCK -> StatusBadge(
            text = "Out of Stock",
            icon = Icons.Default.Cancel,
            textColor = if (isDark) Color(0xFFFF857A) else AlertRed,
            backgroundColor = if (isDark) Color(0xFF3B1714) else AlertRedLight,
            borderColor = if (isDark) Color(0xFF732822) else AlertRedBorder,
            modifier = modifier
        )
    }
}

@Composable
fun UtangStatusBadge(status: UtangStatus, modifier: Modifier = Modifier) {
    val colors = LocalTindaColors.current
    val isDark = colors.isDark

    when (status) {
        UtangStatus.Unpaid -> StatusBadge(
            text = "Unpaid",
            icon = Icons.Default.Schedule,
            textColor = if (isDark) Color(0xFFFF857A) else AlertRed,
            backgroundColor = if (isDark) Color(0xFF3B1714) else AlertRedLight,
            borderColor = if (isDark) Color(0xFF732822) else AlertRedBorder,
            modifier = modifier
        )
        UtangStatus.Partial -> StatusBadge(
            text = "Partial",
            icon = Icons.Default.Pending,
            textColor = if (isDark) Color(0xFFFFB84D) else Amber,
            backgroundColor = if (isDark) Color(0xFF38290D) else AmberLight,
            borderColor = if (isDark) Color(0xFF6B4E1A) else AmberBorder,
            modifier = modifier
        )
        UtangStatus.Paid -> StatusBadge(
            text = "Fully Paid",
            icon = Icons.Default.CheckCircle,
            textColor = if (isDark) Color(0xFF5CD88E) else SuccessGreen,
            backgroundColor = if (isDark) Color(0xFF143825) else SuccessGreenLight,
            borderColor = if (isDark) Color(0xFF235E3D) else SuccessGreenBorder,
            modifier = modifier
        )
    }
}

@Composable
fun ExpiryBadge(urgency: ExpiryUrgency, daysRemaining: Long, modifier: Modifier = Modifier) {
    val colors = LocalTindaColors.current
    val isDark = colors.isDark

    when (urgency) {
        ExpiryUrgency.EXPIRED -> StatusBadge(
            text = "Expired",
            icon = Icons.Default.Dangerous,
            textColor = if (isDark) Color(0xFFFF857A) else AlertRed,
            backgroundColor = if (isDark) Color(0xFF3B1714) else AlertRedLight,
            borderColor = if (isDark) Color(0xFF732822) else AlertRedBorder,
            modifier = modifier
        )
        ExpiryUrgency.EXPIRING_SOON -> StatusBadge(
            text = if (daysRemaining <= 1) "Expires tomorrow!" else "$daysRemaining days left",
            icon = Icons.Default.Timer,
            textColor = if (isDark) Color(0xFFFFB84D) else Amber,
            backgroundColor = if (isDark) Color(0xFF38290D) else AmberLight,
            borderColor = if (isDark) Color(0xFF6B4E1A) else AmberBorder,
            modifier = modifier
        )
        ExpiryUrgency.SAFE -> StatusBadge(
            text = "Good",
            icon = Icons.Default.Check,
            textColor = if (isDark) Color(0xFF5CD88E) else ForestSoft,
            backgroundColor = if (isDark) colors.sageSoft else SageSoft,
            modifier = modifier
        )
    }
}
