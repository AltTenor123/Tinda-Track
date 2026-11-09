package com.example.ui.screens.home

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.model.CurrencyHelper
import com.example.ui.components.*
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToInventory: (filter: String?) -> Unit,
    onNavigateToScan: () -> Unit,
    onNavigateToSales: () -> Unit,
    onNavigateToUtang: () -> Unit,
    onNavigateToMore: () -> Unit,
    onOpenAddProduct: () -> Unit,
    onToggleTheme: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = LocalTindaColors.current

    val todayDateFormatted = remember {
        val sdf = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.US)
        sdf.format(Date())
    }

    var showRestoreConfirmDialog by remember { mutableStateOf(false) }

    if (showRestoreConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreConfirmDialog = false },
            title = {
                Text(
                    text = "Restore Sari-Sari Sample Data?",
                    style = TindaTypography.titleMedium,
                    color = colors.ink
                )
            },
            text = {
                Text(
                    text = "This will reset all 35 sari-sari items, 50 sample sales, and 5 utang records. Any custom changes will be replaced.",
                    style = TindaTypography.bodyMedium,
                    color = colors.muted
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showRestoreConfirmDialog = false
                        viewModel.restoreSampleData()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.forest,
                        contentColor = Color.White
                    )
                ) {
                    Text("Restore Data", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreConfirmDialog = false }) {
                    Text("Cancel", color = colors.muted)
                }
            },
            containerColor = colors.cardCream
        )
    }

    Scaffold(
        containerColor = colors.cream,
        topBar = {
            Surface(
                color = colors.cardCream,
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Tinda Track",
                                style = TindaTypography.headlineLarge,
                                color = colors.ink,
                                fontSize = 22.sp
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(100.dp))
                                    .background(colors.sageSoft)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "OFFLINE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.forest
                                )
                            }
                        }
                        Text(
                            text = todayDateFormatted,
                            style = TindaTypography.bodySmall,
                            color = colors.muted
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Quick Theme Toggle (Sun/Moon)
                        IconButton(
                            onClick = onToggleTheme,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(colors.sageSoft)
                                .border(1.dp, colors.cardBorder, CircleShape)
                                .testTag("theme_toggle_button")
                        ) {
                            Icon(
                                imageVector = if (colors.isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = if (colors.isDark) "Switch to Light Mode" else "Switch to Dark Mode",
                                tint = if (colors.isDark) AccentGold else colors.forest,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Restore Sample Data
                        IconButton(
                            onClick = { showRestoreConfirmDialog = true },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(colors.sageSoft)
                                .border(1.dp, colors.cardBorder, CircleShape)
                                .testTag("restore_sample_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.RestartAlt,
                                contentDescription = "Restore sample data",
                                tint = colors.forest,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Scan shortcut
                        IconButton(
                            onClick = onNavigateToScan,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(colors.forest)
                                .testTag("home_scan_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = "Scan",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = colors.forest)
            }
        } else {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Greeting Banner
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Magandang araw, Nanay Rosa! ☀️",
                            style = TindaTypography.headlineMedium,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.ink
                        )
                        Text(
                            text = "Here is your store summary and counter performance today.",
                            style = TindaTypography.bodySmall,
                            color = colors.muted
                        )
                    }
                }

                // 1. Hero Donut Metric Card
                item {
                    HeroDonutMetricCard(
                        title = "Today's Counter Performance",
                        subtitle = "${uiState.todayTransactionsCount} orders completed today",
                        primaryAmountCentavos = uiState.todaySalesCentavos,
                        badgeText = "+₱${(uiState.todayProfitCentavos / 100.0).toInt()} profit",
                        profitCentavos = uiState.todayProfitCentavos,
                        stockValueCentavos = uiState.stockRetailValueCentavos
                    )
                }

                // 2. Alert Status Pills (Horizontal Scrollable)
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Attention & Inventory Alerts",
                            style = TindaTypography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = colors.ink
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            item {
                                QuickAlertPill(
                                    label = "Low Stock",
                                    count = uiState.lowStockCount,
                                    color = Amber,
                                    backgroundColor = if (colors.isDark) colors.sageSoft else AmberLight,
                                    borderColor = AmberBorder,
                                    onClick = { onNavigateToInventory("LOW_STOCK") },
                                    modifier = Modifier.testTag("pill_low_stock")
                                )
                            }
                            item {
                                QuickAlertPill(
                                    label = "Out of Stock",
                                    count = uiState.outOfStockCount,
                                    color = AlertRed,
                                    backgroundColor = if (colors.isDark) colors.sageSoft else AlertRedLight,
                                    borderColor = AlertRedBorder,
                                    onClick = { onNavigateToInventory("OUT_OF_STOCK") },
                                    modifier = Modifier.testTag("pill_out_of_stock")
                                )
                            }
                            item {
                                QuickAlertPill(
                                    label = "Expiring Soon",
                                    count = uiState.expiringSoonCount,
                                    color = Amber,
                                    backgroundColor = if (colors.isDark) colors.sageSoft else AmberLight,
                                    borderColor = AmberBorder,
                                    onClick = onNavigateToMore,
                                    modifier = Modifier.testTag("pill_expiring")
                                )
                            }
                            item {
                                QuickAlertPill(
                                    label = "Open Utang",
                                    count = uiState.openUtangCount,
                                    color = AccentGold,
                                    backgroundColor = if (colors.isDark) colors.sageSoft else AccentGoldLight,
                                    borderColor = AmberBorder,
                                    onClick = onNavigateToUtang,
                                    modifier = Modifier.testTag("pill_open_utang")
                                )
                            }
                        }
                    }
                }

                // 3. Quick Action Buttons (3 Cards Grid)
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ActionCard(
                            title = "New Sale",
                            subtitle = "Scan / Cart",
                            icon = Icons.Default.ShoppingCart,
                            badgeColor = colors.forest,
                            onClick = onNavigateToScan,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("action_new_sale")
                        )

                        ActionCard(
                            title = "Add Item",
                            subtitle = "New Paninda",
                            icon = Icons.Default.AddBox,
                            badgeColor = colors.sage,
                            onClick = onOpenAddProduct,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("action_add_product")
                        )

                        ActionCard(
                            title = "Utang List",
                            subtitle = CurrencyHelper.formatCentavos(uiState.openUtangTotalCentavos),
                            icon = Icons.Default.Book,
                            badgeColor = AccentGold,
                            onClick = onNavigateToUtang,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("action_record_utang")
                        )
                    }
                }

                // 4. Financial & Stock Overview Cards (2 Columns)
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SummaryStatCard(
                            title = "Open Utang",
                            amountCentavos = uiState.openUtangTotalCentavos,
                            subtitle = "${uiState.openUtangCount} customers owe store",
                            badgeText = "Credit",
                            badgeColor = AccentGold,
                            badgeBgColor = if (colors.isDark) colors.sageSoft else AccentGoldLight,
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.PendingActions,
                                    contentDescription = null,
                                    tint = AccentGold,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToUtang
                        )

                        SummaryStatCard(
                            title = "Stock Capital",
                            amountCentavos = uiState.stockCostValueCentavos,
                            subtitle = "${uiState.totalProductsCount} sari-sari items",
                            badgeText = "Wholesale",
                            badgeColor = colors.forest,
                            badgeBgColor = colors.sageSoft,
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Inventory,
                                    contentDescription = null,
                                    tint = colors.forest,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigateToInventory(null) }
                        )
                    }
                }

                // 5. Recent Activity Feed (Sales & Transactions)
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recent Transactions",
                            style = TindaTypography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = colors.ink
                        )
                        TextButton(onClick = onNavigateToSales) {
                            Text(
                                text = "View All (${uiState.todayTransactionsCount})",
                                color = colors.forest,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                if (uiState.recentSales.isEmpty()) {
                    item {
                        EmptyStateView(
                            icon = Icons.AutoMirrored.Filled.ReceiptLong,
                            title = "No sales yet today",
                            description = "Scan items or use the cart to record your first transaction."
                        )
                    }
                } else {
                    items(uiState.recentSales) { sale ->
                        Surface(
                            onClick = onNavigateToSales,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = colors.cardCream,
                            border = BorderStroke(1.dp, colors.cardBorder),
                            shadowElevation = 1.dp
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(colors.sageSoft),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Receipt,
                                            contentDescription = null,
                                            tint = colors.forest,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text(
                                            text = sale.receiptNo,
                                            style = TindaTypography.labelLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = colors.ink
                                        )
                                        Text(
                                            text = "${sale.items.size} item(s) • ${sale.cashier} • ${sale.createdAt.take(16).replace("T", " ")}",
                                            style = TindaTypography.bodySmall,
                                            color = colors.muted,
                                            maxLines = 1
                                        )
                                    }
                                }

                                Column(
                                    horizontalAlignment = Alignment.End,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = colors.sageSoft
                                    ) {
                                        Text(
                                            text = CurrencyHelper.formatCentavos(sale.total),
                                            style = TindaTypography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = colors.forest,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                    Text(
                                        text = "+${CurrencyHelper.formatCentavos(sale.estimatedProfit)} profit",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = colors.success
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun ActionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    badgeColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalTindaColors.current
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = colors.cardCream,
        border = BorderStroke(1.dp, colors.cardBorder),
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(vertical = 14.dp, horizontal = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(colors.sageSoft),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = badgeColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            Text(
                text = title,
                style = TindaTypography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = colors.ink,
                maxLines = 1
            )
            Text(
                text = subtitle,
                style = TindaTypography.bodySmall,
                color = colors.muted,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun QuickAlertPill(
    label: String,
    count: Int,
    color: Color,
    backgroundColor: Color,
    borderColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(100.dp),
        color = backgroundColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Text(
                text = "$label: $count",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}
