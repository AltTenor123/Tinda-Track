package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun HeroDonutMetricCard(
    title: String,
    primaryAmountCentavos: Long,
    subtitle: String,
    badgeText: String,
    profitCentavos: Long,
    stockValueCentavos: Long,
    modifier: Modifier = Modifier
) {
    val colors = LocalTindaColors.current
    TindaCard(
        modifier = modifier.fillMaxWidth(),
        backgroundColor = colors.cardCream,
        borderColor = colors.cardBorder,
        elevation = 2.dp,
        shape = RoundedCornerShape(22.dp),
        contentPadding = PaddingValues(18.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = title,
                        style = TindaTypography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.ink
                    )
                    Text(
                        text = subtitle,
                        style = TindaTypography.bodySmall,
                        color = colors.muted
                    )
                }

                Surface(
                    shape = RoundedCornerShape(100.dp),
                    color = colors.sageSoft,
                    border = BorderStroke(1.dp, colors.sage.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                            contentDescription = null,
                            tint = colors.forest,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = badgeText,
                            color = colors.forest,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Donut & Primary Metric Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // Donut Visual
                Box(
                    modifier = Modifier.size(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(92.dp)) {
                        val strokeWidth = 11.dp.toPx()
                        drawCircle(
                            color = if (colors.isDark) Color(0xFF1B382D) else CreamDark,
                            style = Stroke(width = strokeWidth)
                        )
                        // Segments inspired by dashboard design
                        drawArc(
                            color = colors.forest,
                            startAngle = -90f,
                            sweepAngle = 200f,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                        drawArc(
                            color = colors.sage,
                            startAngle = 120f,
                            sweepAngle = 70f,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                        drawArc(
                            color = colors.gold,
                            startAngle = 200f,
                            sweepAngle = 50f,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "TODAY",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.muted,
                            letterSpacing = 1.sp
                        )
                        Icon(
                            imageVector = Icons.Default.Storefront,
                            contentDescription = null,
                            tint = colors.forest,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Gross Sales",
                        style = TindaTypography.labelMedium,
                        color = colors.muted
                    )
                    CurrencyText(
                        centavos = primaryAmountCentavos,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.ink
                    )
                    Text(
                        text = "Real-time ledger updates",
                        style = TindaTypography.bodySmall,
                        color = colors.muted
                    )
                }
            }

            // Sub Stats Row (Net profit & Retail inventory value)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    color = if (colors.isDark) colors.sageSoft else colors.cream,
                    border = BorderStroke(1.dp, colors.cardBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "Est. Net Profit",
                            style = TindaTypography.labelSmall,
                            color = colors.muted
                        )
                        CurrencyText(
                            centavos = profitCentavos,
                            fontSize = 16.sp,
                            color = colors.success
                        )
                    }
                }

                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    color = if (colors.isDark) colors.sageSoft else colors.cream,
                    border = BorderStroke(1.dp, colors.cardBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "Retail Stock Value",
                            style = TindaTypography.labelSmall,
                            color = colors.muted
                        )
                        CurrencyText(
                            centavos = stockValueCentavos,
                            fontSize = 16.sp,
                            color = colors.ink
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryStatCard(
    title: String,
    amountCentavos: Long,
    subtitle: String,
    badgeText: String,
    badgeColor: Color,
    badgeBgColor: Color,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val colors = LocalTindaColors.current
    val surfaceShape = RoundedCornerShape(20.dp)

    if (onClick != null) {
        Surface(
            onClick = onClick,
            modifier = modifier.fillMaxWidth(),
            shape = surfaceShape,
            color = colors.cardCream,
            border = BorderStroke(1.dp, colors.cardBorder),
            shadowElevation = 1.dp
        ) {
            StatCardContent(
                title = title,
                amountCentavos = amountCentavos,
                subtitle = subtitle,
                badgeText = badgeText,
                badgeColor = badgeColor,
                badgeBgColor = badgeBgColor,
                icon = icon
            )
        }
    } else {
        Surface(
            modifier = modifier.fillMaxWidth(),
            shape = surfaceShape,
            color = colors.cardCream,
            border = BorderStroke(1.dp, colors.cardBorder),
            shadowElevation = 1.dp
        ) {
            StatCardContent(
                title = title,
                amountCentavos = amountCentavos,
                subtitle = subtitle,
                badgeText = badgeText,
                badgeColor = badgeColor,
                badgeBgColor = badgeBgColor,
                icon = icon
            )
        }
    }
}

@Composable
private fun StatCardContent(
    title: String,
    amountCentavos: Long,
    subtitle: String,
    badgeText: String,
    badgeColor: Color,
    badgeBgColor: Color,
    icon: @Composable () -> Unit
) {
    val colors = LocalTindaColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Top Row: Icon on left, Pill badge on right
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(colors.sageSoft),
                contentAlignment = Alignment.Center
            ) {
                icon()
            }

            Surface(
                shape = RoundedCornerShape(100.dp),
                color = badgeBgColor,
                border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.3f))
            ) {
                Text(
                    text = badgeText,
                    color = badgeColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }

        // Title below icon
        Text(
            text = title,
            style = TindaTypography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = colors.ink,
            maxLines = 1
        )

        // Amount in Pesos (High contrast)
        CurrencyText(
            centavos = amountCentavos,
            fontSize = 20.sp,
            color = colors.forest
        )

        // Subtitle
        Text(
            text = subtitle,
            style = TindaTypography.bodySmall,
            color = colors.muted,
            maxLines = 1
        )
    }
}

@Composable
fun AlertCountCard(
    count: Int,
    label: String,
    sublabel: String,
    accentColor: Color,
    bgColor: Color,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val colors = LocalTindaColors.current
    val surfaceShape = RoundedCornerShape(18.dp)

    if (onClick != null) {
        Surface(
            onClick = onClick,
            modifier = modifier.fillMaxWidth(),
            shape = surfaceShape,
            color = colors.cardCream,
            border = BorderStroke(1.dp, accentColor.copy(alpha = 0.35f)),
            shadowElevation = 1.dp
        ) {
            AlertCardContent(
                count = count,
                label = label,
                sublabel = sublabel,
                accentColor = accentColor,
                bgColor = bgColor,
                icon = icon
            )
        }
    } else {
        Surface(
            modifier = modifier.fillMaxWidth(),
            shape = surfaceShape,
            color = colors.cardCream,
            border = BorderStroke(1.dp, accentColor.copy(alpha = 0.35f)),
            shadowElevation = 1.dp
        ) {
            AlertCardContent(
                count = count,
                label = label,
                sublabel = sublabel,
                accentColor = accentColor,
                bgColor = bgColor,
                icon = icon
            )
        }
    }
}

@Composable
private fun AlertCardContent(
    count: Int,
    label: String,
    sublabel: String,
    accentColor: Color,
    bgColor: Color,
    icon: @Composable () -> Unit
) {
    val colors = LocalTindaColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            icon()
        }

        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = count.toString(),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor,
                    fontFamily = FrauncesFontFamily
                )
                Text(
                    text = label,
                    style = TindaTypography.titleSmall,
                    color = colors.ink,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = sublabel,
                style = TindaTypography.bodySmall,
                color = colors.muted
            )
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = colors.muted,
            modifier = Modifier.size(20.dp)
        )
    }
}
