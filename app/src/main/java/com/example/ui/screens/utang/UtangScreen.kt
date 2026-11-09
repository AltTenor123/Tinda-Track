package com.example.ui.screens.utang

import androidx.compose.animation.*
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
import com.example.domain.model.Utang
import com.example.domain.model.UtangStatus
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun UtangScreen(
    viewModel: UtangViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
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
            .testTag("utang_screen"),
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Utang Ledger (Credit)",
                                style = TindaTypography.titleLarge,
                                color = Ink
                            )
                            Text(
                                text = "Customer store credit & payment log",
                                style = TindaTypography.bodySmall,
                                color = Muted
                            )
                        }

                        // Add Utang Button
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { viewModel.openAddUtang() }
                                .testTag("open_add_utang_button"),
                            shape = RoundedCornerShape(16.dp),
                            color = Forest,
                            shadowElevation = 4.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add Utang",
                                    tint = CreamLight,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Add Utang",
                                    style = TindaTypography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = CreamLight
                                )
                            }
                        }
                    }

                    // Search field
                    TindaSearchField(
                        query = uiState.searchQuery,
                        onQueryChange = { viewModel.onSearchQueryChanged(it) },
                        placeholder = "Search customer name or items...",
                        modifier = Modifier.testTag("utang_search_field")
                    )

                    // Filter chips: All, Unpaid, Partial, Paid
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            UtangFilterChip(
                                label = "All (${uiState.allUtang.size})",
                                isSelected = uiState.selectedStatusFilter == null,
                                onClick = { viewModel.onStatusFilterChanged(null) }
                            )
                        }
                        item {
                            UtangFilterChip(
                                label = "Unpaid",
                                isSelected = uiState.selectedStatusFilter == UtangStatus.Unpaid,
                                onClick = { viewModel.onStatusFilterChanged(UtangStatus.Unpaid) }
                            )
                        }
                        item {
                            UtangFilterChip(
                                label = "Partial",
                                isSelected = uiState.selectedStatusFilter == UtangStatus.Partial,
                                onClick = { viewModel.onStatusFilterChanged(UtangStatus.Partial) }
                            )
                        }
                        item {
                            UtangFilterChip(
                                label = "Paid",
                                isSelected = uiState.selectedStatusFilter == UtangStatus.Paid,
                                onClick = { viewModel.onStatusFilterChanged(UtangStatus.Paid) }
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // Open Credit Summary Card
            item {
                SummaryStatCard(
                    title = "TOTAL OPEN UTANG",
                    amountCentavos = uiState.openCreditTotalCentavos,
                    subtitle = "Collectable receivables from store customers",
                    badgeText = "${uiState.allUtang.count { it.status != UtangStatus.Paid }} active",
                    badgeColor = AlertRed,
                    badgeBgColor = AlertRedLight,
                    icon = {
                        Icon(
                            imageVector = Icons.Default.PendingActions,
                            contentDescription = null,
                            tint = Forest,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
            }

            // Records List
            if (uiState.filteredUtang.isEmpty() && !uiState.isLoading) {
                item {
                    EmptyStateView(
                        icon = Icons.Default.CheckCircle,
                        title = "No Utang Records",
                        description = if (uiState.searchQuery.isNotBlank()) "No records match \"${uiState.searchQuery}\"" else "All customers are fully paid! Tap 'Add Utang' to record a new credit."
                    )
                }
            } else {
                items(uiState.filteredUtang, key = { it.id }) { utang ->
                    UtangRecordCard(
                        utang = utang,
                        onRecordPayment = { viewModel.openRecordPayment(utang) },
                        onDelete = { viewModel.deleteUtang(utang.id) }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }

    // Add Utang Dialog
    if (uiState.isAddUtangOpen) {
        AddUtangDialog(
            onDismiss = { viewModel.closeAddUtang() },
            onConfirm = { name, note, amount ->
                viewModel.createUtang(name, note, amount)
            }
        )
    }

    // Record Payment Dialog
    uiState.activeUtangForPayment?.let { utang ->
        RecordPaymentDialog(
            utang = utang,
            payments = uiState.paymentHistoryForActive,
            errorMessage = uiState.paymentErrorMessage,
            onDismiss = { viewModel.closeRecordPayment() },
            onConfirmPayment = { paymentCentavos ->
                viewModel.submitPayment(paymentCentavos)
            }
        )
    }
}

@Composable
private fun UtangRecordCard(
    utang: Utang,
    onRecordPayment: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
            .testTag("utang_card_${utang.id}"),
        shape = RoundedCornerShape(16.dp),
        color = CardCream
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header: Customer name & Status badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = utang.customerName,
                        style = TindaTypography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Ink
                    )
                    Text(
                        text = utang.createdAt.take(16).replace("T", " "),
                        style = TindaTypography.bodySmall,
                        color = Muted
                    )
                }

                UtangStatusBadge(status = utang.status)
            }

            // Items description if any
            if (utang.itemsNote.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Cream,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = utang.itemsNote,
                        style = TindaTypography.bodySmall,
                        color = Ink,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            // Amounts Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Total Credit", style = TindaTypography.labelSmall, color = Muted)
                    Text(
                        text = CurrencyHelper.formatCentavos(utang.amount),
                        style = TindaTypography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Ink
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Paid", style = TindaTypography.labelSmall, color = Muted)
                    Text(
                        text = CurrencyHelper.formatCentavos(utang.paid),
                        style = TindaTypography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = SageDeep
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Remaining Balance", style = TindaTypography.labelSmall, color = Muted)
                    Text(
                        text = CurrencyHelper.formatCentavos(utang.balance),
                        style = TindaTypography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (utang.balance > 0) Amber else SuccessGreen
                    )
                }
            }

            Divider(color = SageSoft, thickness = 1.dp)

            // Actions: Record Bayad & Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { showDeleteConfirm = true },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete record",
                        tint = AlertRed,
                        modifier = Modifier.size(18.dp)
                    )
                }

                if (utang.status != UtangStatus.Paid) {
                    Button(
                        onClick = onRecordPayment,
                        colors = ButtonDefaults.buttonColors(containerColor = Forest),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("record_bayad_button_${utang.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Payments,
                            contentDescription = null,
                            tint = CreamLight,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Record Bayad",
                            style = TindaTypography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = CreamLight
                        )
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = SuccessGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Fully Paid",
                            style = TindaTypography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = SuccessGreen
                        )
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Utang Record?", style = TindaTypography.titleMedium) },
            text = {
                Text(
                    "Are you sure you want to delete the record for ${utang.customerName} (${CurrencyHelper.formatCentavos(utang.amount)})?",
                    style = TindaTypography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete()
                    }
                ) {
                    Text("Delete", color = AlertRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel", color = Muted)
                }
            },
            containerColor = CardCream
        )
    }
}

@Composable
private fun UtangFilterChip(
    label: String,
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
            text = label,
            style = TindaTypography.labelSmall,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) CreamLight else Ink,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
        )
    }
}
