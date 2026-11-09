package com.example.ui.screens.sales

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.domain.model.Sale
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun SalesScreen(
    viewModel: SalesViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val cartItems by viewModel.cartManager.items.collectAsStateWithLifecycle()
    val cartCount = viewModel.cartManager.itemCount
    val cartTotal = viewModel.cartManager.totalCentavos

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
            .testTag("sales_screen"),
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
                                text = "Sales & Transactions",
                                style = TindaTypography.titleLarge,
                                color = Ink
                            )
                            Text(
                                text = "Daily receipts & counter revenue",
                                style = TindaTypography.bodySmall,
                                color = Muted
                            )
                        }

                        // Cart / Checkout Button
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { viewModel.openCart() }
                                .testTag("open_cart_button"),
                            shape = RoundedCornerShape(16.dp),
                            color = Forest,
                            shadowElevation = 4.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingCart,
                                    contentDescription = "Cart",
                                    tint = CreamLight,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = if (cartCount > 0) "$cartCount (${CurrencyHelper.formatCentavos(cartTotal)})" else "Cart",
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
                        placeholder = "Search receipt #, item, cashier...",
                        modifier = Modifier.testTag("sales_search_field")
                    )
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // Hero Summary Card: Today's Revenue & Profit
            item {
                SummaryStatCard(
                    title = "TODAY'S SALES",
                    amountCentavos = uiState.todaySummary.totalRevenueCentavos,
                    subtitle = "${uiState.todaySummary.transactionCount} transactions recorded today",
                    badgeText = "Profit: +${CurrencyHelper.formatCentavos(uiState.todaySummary.estimatedProfitCentavos)}",
                    badgeColor = Forest,
                    badgeBgColor = SageSoft,
                    icon = {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = Forest,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
            }

            // All-Time Summary Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SummaryStatCard(
                        title = "ALL-TIME SALES",
                        amountCentavos = uiState.totalSummary.totalRevenueCentavos,
                        subtitle = "${uiState.totalSummary.transactionCount} orders",
                        badgeText = "Gross",
                        badgeColor = SageDeep,
                        badgeBgColor = SageSoft,
                        icon = {
                            Icon(
                                imageVector = Icons.Default.PointOfSale,
                                contentDescription = null,
                                tint = Forest,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        modifier = Modifier.weight(1f)
                    )

                    SummaryStatCard(
                        title = "TOTAL PROFIT",
                        amountCentavos = uiState.totalSummary.estimatedProfitCentavos,
                        subtitle = "Net store margin",
                        badgeText = "Net Margin",
                        badgeColor = SuccessGreen,
                        badgeBgColor = SuccessGreenLight,
                        icon = {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = Forest,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Sales Grouped by Day
            if (uiState.filteredSales.isEmpty() && !uiState.isLoading) {
                item {
                    EmptyStateView(
                        icon = Icons.Default.ReceiptLong,
                        title = "No Sales Found",
                        description = if (uiState.searchQuery.isNotBlank()) "No transactions match \"${uiState.searchQuery}\"" else "No sales recorded yet. Sell items via the Scan tab or Cart!"
                    )
                }
            } else {
                uiState.groupedSales.forEach { (dateHeader, sales) ->
                    item {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = SageSoft,
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text(
                                text = dateHeader,
                                style = TindaTypography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Forest,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    items(sales, key = { it.id }) { sale ->
                        SaleItemCard(
                            sale = sale,
                            onClick = { viewModel.viewReceipt(sale) }
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }

    // Cart / Checkout Dialog
    if (uiState.isCartOpen) {
        CartCheckoutDialog(
            cartManager = viewModel.cartManager,
            onCheckout = { cashReceived ->
                viewModel.performCheckout(cashReceived)
            },
            onDismiss = { viewModel.closeCart() },
            errorMessage = uiState.checkoutError,
            isCheckingOut = uiState.isCheckingOut
        )
    }

    // Receipt Modal Dialog
    uiState.activeReceiptSale?.let { sale ->
        ReceiptDialog(
            sale = sale,
            onDismiss = { viewModel.dismissReceipt() }
        )
    }
}

@Composable
private fun SaleItemCard(
    sale: Sale,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
            .testTag("sale_card_${sale.receiptNo}"),
        shape = RoundedCornerShape(16.dp),
        color = CardCream
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Receipt Header
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
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(SageSoft),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Receipt,
                            contentDescription = null,
                            tint = Forest,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column {
                        Text(
                            text = sale.receiptNo,
                            style = TindaTypography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = Ink
                        )
                        Text(
                            text = "${sale.createdAt.take(16).replace("T", " ")} • ${sale.cashier}",
                            style = TindaTypography.bodySmall,
                            color = Muted
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = CurrencyHelper.formatCentavos(sale.total),
                        style = TindaTypography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Forest
                    )
                    Text(
                        text = "+${CurrencyHelper.formatCentavos(sale.estimatedProfit)} profit",
                        style = TindaTypography.labelSmall,
                        color = SageDeep,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Divider(color = SageSoft, thickness = 1.dp)

            val itemsPreview = sale.items.joinToString(", ") { "${it.quantity}x ${it.productName}" }
            Text(
                text = itemsPreview,
                style = TindaTypography.bodySmall,
                color = Ink,
                maxLines = 2
            )
        }
    }
}
