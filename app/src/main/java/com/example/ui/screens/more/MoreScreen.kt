package com.example.ui.screens.more

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.model.CurrencyHelper
import com.example.domain.usecase.ExpiryUrgency
import com.example.ui.components.*
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun MoreScreen(
    viewModel: MoreViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.dismissSnackbar()
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("more_screen"),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            Surface(
                color = CardCream,
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Store Operations & Reports",
                        style = TindaTypography.titleLarge,
                        color = Ink
                    )

                    // SubTabs Row
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            MoreSubTabPill(
                                title = "📦 Restock (${uiState.totalLowStockProducts})",
                                isSelected = uiState.activeSubTab == MoreSubTab.REORDER,
                                onClick = { viewModel.setSubTab(MoreSubTab.REORDER) }
                            )
                        }
                        item {
                            MoreSubTabPill(
                                title = "⏳ Expiry (${uiState.totalExpiringProducts})",
                                isSelected = uiState.activeSubTab == MoreSubTab.EXPIRY,
                                onClick = { viewModel.setSubTab(MoreSubTab.EXPIRY) }
                            )
                        }
                        item {
                            MoreSubTabPill(
                                title = "💾 Data Backup",
                                isSelected = uiState.activeSubTab == MoreSubTab.DATA_BACKUP,
                                onClick = { viewModel.setSubTab(MoreSubTab.DATA_BACKUP) }
                            )
                        }
                        item {
                            MoreSubTabPill(
                                title = "⚙️ Settings",
                                isSelected = uiState.activeSubTab == MoreSubTab.SETTINGS,
                                onClick = { viewModel.setSubTab(MoreSubTab.SETTINGS) }
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (uiState.activeSubTab) {
                MoreSubTab.REORDER -> ReorderListView(
                    viewModel = viewModel,
                    uiState = uiState,
                    context = context
                )
                MoreSubTab.EXPIRY -> ExpiryAlertsView(
                    uiState = uiState
                )
                MoreSubTab.DATA_BACKUP -> DataBackupView(
                    viewModel = viewModel,
                    context = context
                )
                MoreSubTab.SETTINGS, MoreSubTab.ABOUT -> SettingsView(
                    viewModel = viewModel,
                    uiState = uiState
                )
            }
        }
    }
}

@Composable
private fun ReorderListView(
    viewModel: MoreViewModel,
    uiState: MoreUiState,
    context: Context
) {
    val colors = LocalTindaColors.current
    val coroutineScope = rememberCoroutineScope()
    val grandTotalCentavos = uiState.reorderGroups.sumOf { it.totalEstimatedCostCentavos }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Summary & Copy As Text Button
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = CardCream,
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Estimated Restock Budget",
                                style = TindaTypography.labelMedium,
                                color = Muted
                            )
                            Text(
                                text = CurrencyHelper.formatCentavos(grandTotalCentavos),
                                style = TindaTypography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = Forest
                            )
                        }

                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    val text = viewModel.getFormattedReorderText()
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Restock List", text)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "Reorder list copied to clipboard!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Forest),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Copy as Text", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Text(
                        text = "Orders calculated to buffer up to 2x reorder level. Copy directly to SMS or Viber to send to suppliers.",
                        style = TindaTypography.bodySmall,
                        color = Muted
                    )
                }
            }
        }

        if (uiState.reorderGroups.isEmpty()) {
            item {
                EmptyStateView(
                    icon = Icons.Default.CheckCircle,
                    title = "All Products Well-Stocked",
                    description = "No items are currently below their reorder threshold. Store inventory is healthy!"
                )
            }
        } else {
            items(uiState.reorderGroups) { group ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = colors.cardCream,
                    border = BorderStroke(1.dp, colors.cardBorder),
                    shadowElevation = 1.dp
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(colors.sageSoft),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocalShipping,
                                        contentDescription = null,
                                        tint = colors.forest,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Text(
                                    text = group.supplier,
                                    style = TindaTypography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.ink
                                )
                            }

                            Text(
                                text = "Subtotal: ${CurrencyHelper.formatCentavos(group.totalEstimatedCostCentavos)}",
                                style = TindaTypography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = colors.forest
                            )
                        }

                        HorizontalDivider(color = colors.cardBorder, thickness = 1.dp)

                        group.items.forEach { item ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = item.product.name,
                                            style = TindaTypography.labelMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Ink
                                        )
                                        if (item.product.quantity == 0) {
                                            Text(
                                                text = "OUT",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = AlertRed
                                            )
                                        } else {
                                            Text(
                                                text = "(${item.product.quantity} left)",
                                                fontSize = 11.sp,
                                                color = Amber
                                            )
                                        }
                                    }
                                    Text(
                                        text = "Order: ${item.suggestedQuantity} pcs @ ${CurrencyHelper.formatCentavos(item.product.costPrice)} each",
                                        style = TindaTypography.bodySmall,
                                        color = Muted
                                    )
                                }

                                Text(
                                    text = CurrencyHelper.formatCentavos(item.estimatedCostCentavos),
                                    style = TindaTypography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Ink
                                )
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(30.dp)) }
    }
}

@Composable
private fun ExpiryAlertsView(
    uiState: MoreUiState
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Products Expiring Within 30 Days (${uiState.expiryAlerts.size})",
                style = TindaTypography.titleSmall,
                color = Ink
            )
        }

        if (uiState.expiryAlerts.isEmpty()) {
            item {
                EmptyStateView(
                    icon = Icons.Default.CheckCircle,
                    title = "No Expiring Products",
                    description = "All products with expiry dates are fresh for at least 30 days."
                )
            }
        } else {
            items(uiState.expiryAlerts) { alert ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CardBorder, RoundedCornerShape(14.dp)),
                    shape = RoundedCornerShape(14.dp),
                    color = CardCream
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = alert.product.name,
                                style = TindaTypography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = Ink
                            )
                            Text(
                                text = "Expiry Date: ${alert.expiryDate} • Stock: ${alert.product.quantity} pcs",
                                style = TindaTypography.bodySmall,
                                color = Muted
                            )
                        }

                        if (alert.urgency == ExpiryUrgency.EXPIRED) {
                            Surface(
                                shape = RoundedCornerShape(100.dp),
                                color = AlertRedLight,
                                border = androidx.compose.foundation.BorderStroke(1.dp, AlertRedBorder)
                            ) {
                                Text(
                                    text = "EXPIRED",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AlertRed,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        } else {
                            Surface(
                                shape = RoundedCornerShape(100.dp),
                                color = AmberLight,
                                border = androidx.compose.foundation.BorderStroke(1.dp, AmberBorder)
                            ) {
                                Text(
                                    text = "${alert.daysRemaining} days left",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Amber,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(30.dp)) }
    }
}

@Composable
private fun DataBackupView(
    viewModel: MoreViewModel,
    context: Context
) {
    val colors = LocalTindaColors.current
    var showRestoreConfirm by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Offline Data Backup & CSV Export",
                style = TindaTypography.titleMedium,
                color = Ink
            )
            Text(
                text = "Export standard CSV spreadsheets for Excel or store accounting.",
                style = TindaTypography.bodySmall,
                color = Muted
            )
        }

        // Export Cards
        item {
            BackupActionCard(
                title = "Export Products Inventory",
                description = "Download products CSV (SKU, QR, Prices, Stock counts)",
                icon = Icons.Default.Inventory2,
                buttonText = "Export Products CSV",
                onClick = { viewModel.exportProducts(context) }
            )
        }

        item {
            BackupActionCard(
                title = "Export Sales History",
                description = "Download transactions CSV (Receipts, Totals, Profits, Cashiers)",
                icon = Icons.AutoMirrored.Filled.ReceiptLong,
                buttonText = "Export Sales CSV",
                onClick = { viewModel.exportSales(context) }
            )
        }

        item {
            BackupActionCard(
                title = "Export Utang Ledger",
                description = "Download customer credit ledger (Balances, Payments, Status)",
                icon = Icons.Default.PendingActions,
                buttonText = "Export Utang CSV",
                onClick = { viewModel.exportUtang(context) }
            )
        }

        // Restore Sample Data
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = if (colors.isDark) colors.sageSoft else AmberLight,
                border = BorderStroke(1.dp, AmberBorder),
                shadowElevation = 1.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Reset / Restore Sample Sari-Sari Store Data",
                        style = TindaTypography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = colors.ink
                    )
                    Text(
                        text = "Restores 35 realistic Philippine sari-sari items, 50 transactions, and 5 utang records.",
                        style = TindaTypography.bodySmall,
                        color = colors.muted
                    )
                    Button(
                        onClick = { showRestoreConfirm = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Amber),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Reset Store Data", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(30.dp)) }
    }

    if (showRestoreConfirm) {
        AlertDialog(
            onDismissRequest = { showRestoreConfirm = false },
            title = { Text("Restore Sample Data?") },
            text = { Text("This will reload sample items, sales, and utang records.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRestoreConfirm = false
                        viewModel.restoreSampleData(context)
                    }
                ) {
                    Text("Confirm", color = AlertRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreConfirm = false }) {
                    Text("Cancel", color = Muted)
                }
            },
            containerColor = CardCream
        )
    }
}

@Composable
private fun BackupActionCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    buttonText: String,
    onClick: () -> Unit
) {
    val colors = LocalTindaColors.current
    Surface(
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
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(colors.sageSoft),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = colors.forest, modifier = Modifier.size(20.dp))
                }
                Column {
                    Text(text = title, style = TindaTypography.titleSmall, color = colors.ink)
                    Text(text = description, style = TindaTypography.bodySmall, color = colors.muted)
                }
            }

            IconButton(
                onClick = onClick,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(colors.forest)
            ) {
                Icon(imageVector = Icons.Default.Share, contentDescription = buttonText, tint = CreamLight, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
private fun SettingsView(
    viewModel: MoreViewModel,
    uiState: MoreUiState
) {
    val colors = LocalTindaColors.current
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Store & App Settings",
                style = TindaTypography.titleMedium,
                color = colors.ink
            )
        }

        // App PIN Lock
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = colors.cardCream,
                border = BorderStroke(1.dp, colors.cardBorder),
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(colors.sageSoft),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = colors.forest, modifier = Modifier.size(20.dp))
                        }
                        Column {
                            Text(text = "App Passcode Lock", style = TindaTypography.titleSmall, color = colors.ink)
                            Text(
                                text = "Auto-locks after 15 minutes in background (Default PIN: 1234)",
                                style = TindaTypography.bodySmall,
                                color = colors.muted
                            )
                        }
                    }

                    Switch(
                        checked = uiState.isPasscodeEnabled,
                        onCheckedChange = { viewModel.togglePasscode(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = colors.forest, checkedTrackColor = colors.sageSoft)
                    )
                }
            }
        }

        // Theme Setting
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = colors.cardCream,
                border = BorderStroke(1.dp, colors.cardBorder),
                shadowElevation = 1.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(text = "Appearance Theme", style = TindaTypography.titleSmall, color = colors.ink)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ThemeOptionButton(
                            label = "System",
                            isSelected = uiState.themeSetting == "SYSTEM",
                            onClick = { viewModel.setTheme("SYSTEM") },
                            modifier = Modifier.weight(1f)
                        )
                        ThemeOptionButton(
                            label = "Warm Cream",
                            isSelected = uiState.themeSetting == "LIGHT",
                            onClick = { viewModel.setTheme("LIGHT") },
                            modifier = Modifier.weight(1f)
                        )
                        ThemeOptionButton(
                            label = "Forest Dark",
                            isSelected = uiState.themeSetting == "DARK",
                            onClick = { viewModel.setTheme("DARK") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // About Tinda Track
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = colors.cardCream,
                border = BorderStroke(1.dp, colors.cardBorder),
                shadowElevation = 1.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "About Tinda Track",
                        style = TindaTypography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = colors.ink
                    )
                    Text(
                        text = "Tinda Track is an offline-first inventory, QR scanner, and sales tracker tailor-made for Philippine sari-sari stores.",
                        style = TindaTypography.bodySmall,
                        color = colors.muted
                    )
                    HorizontalDivider(color = colors.cardBorder, thickness = 1.dp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Currency", style = TindaTypography.bodySmall, color = Muted)
                        Text(text = "Philippine Peso (₱)", style = TindaTypography.bodySmall, fontWeight = FontWeight.Bold, color = Ink)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Store Mode", style = TindaTypography.bodySmall, color = Muted)
                        Text(text = "Single-Device Offline SQLite", style = TindaTypography.bodySmall, fontWeight = FontWeight.Bold, color = Forest)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Version", style = TindaTypography.bodySmall, color = Muted)
                        Text(text = "v1.0 Production", style = TindaTypography.bodySmall, fontWeight = FontWeight.Bold, color = Ink)
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(30.dp)) }
    }
}

@Composable
private fun MoreSubTabPill(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) Forest else CardCream,
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) Forest else CardBorder)
    ) {
        Text(
            text = title,
            style = TindaTypography.labelSmall,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) CreamLight else Ink,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun ThemeOptionButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) Forest else Cream,
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) Forest else CardBorder)
    ) {
        Text(
            text = label,
            style = TindaTypography.labelSmall,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) CreamLight else Ink,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(vertical = 10.dp)
        )
    }
}
