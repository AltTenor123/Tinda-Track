package com.example.ui.screens.inventory

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import com.example.domain.model.CurrencyHelper
import com.example.domain.model.Product
import com.example.domain.model.StockStatus
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun InventoryScreen(
    viewModel: InventoryViewModel,
    initialStatusFilter: String? = null,
    onNavigateToQrLabel: (Product) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = LocalTindaColors.current

    var productToDelete by remember { mutableStateOf<Product?>(null) }

    LaunchedEffect(initialStatusFilter) {
        if (!initialStatusFilter.isNullOrBlank()) {
            val status = try {
                StockStatus.valueOf(initialStatusFilter)
            } catch (_: Exception) {
                null
            }
            viewModel.onStatusFilterChanged(status)
        }
    }

    // Delete Confirmation Dialog
    if (productToDelete != null) {
        AlertDialog(
            onDismissRequest = { productToDelete = null },
            title = {
                Text(
                    text = "Delete '${productToDelete?.name}'?",
                    style = TindaTypography.titleMedium,
                    color = colors.ink
                )
            },
            text = {
                Text(
                    text = "This item will be removed from your active store inventory. Past sales and receipts that sold this item will remain intact.",
                    style = TindaTypography.bodyMedium,
                    color = colors.muted
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        productToDelete?.let { viewModel.deleteProduct(it.id, it.name) }
                        productToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AlertRed,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.testTag("confirm_delete_button")
                ) {
                    Text("Delete Item", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { productToDelete = null }) {
                    Text("Cancel", color = colors.muted)
                }
            },
            containerColor = colors.cardCream
        )
    }

    // Add / Edit Product Sheet
    if (uiState.isFormOpen) {
        ProductFormSheet(
            product = uiState.activeProductForEdit,
            availableCategories = uiState.availableCategories,
            availableOwners = uiState.availableOwners,
            onDismiss = { viewModel.closeProductForm() },
            onSave = { id, name, owner, cat, supp, sku, qr, cost, price, qty, reorder, exp, emoji ->
                viewModel.saveProduct(id, name, owner, cat, supp, sku, qr, cost, price, qty, reorder, exp, emoji)
            }
        )
    }

    // Stock Movement Adjust Dialog
    if (uiState.activeProductForAdjustment != null) {
        StockAdjustDialog(
            product = uiState.activeProductForAdjustment!!,
            onDismiss = { viewModel.closeStockAdjustment() },
            onConfirm = { type, count, reason ->
                viewModel.applyStockChange(uiState.activeProductForAdjustment!!.id, type, count, reason)
            }
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
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Store Inventory",
                            style = TindaTypography.headlineLarge,
                            color = colors.ink,
                            fontSize = 24.sp
                        )
                        Text(
                            text = "${uiState.filteredProducts.size} of ${uiState.totalCount} products",
                            style = TindaTypography.bodySmall,
                            color = colors.muted
                        )
                    }

                    Button(
                        onClick = { viewModel.openProductForm(null) },
                        shape = RoundedCornerShape(100.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.forest,
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                        modifier = Modifier.testTag("add_product_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Add Item",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        },
        snackbarHost = {
            uiState.snackbarMessage?.let { msg ->
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    action = {
                        TextButton(onClick = { viewModel.dismissSnackbar() }) {
                            Text("OK", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    },
                    containerColor = colors.forest,
                    contentColor = Color.White
                ) {
                    Text(msg, color = Color.White)
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search Input Field
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                TindaSearchField(
                    query = uiState.searchQuery,
                    onQueryChange = { viewModel.onSearchQueryChanged(it) },
                    placeholder = "Search name, SKU, QR, category, owner...",
                    modifier = Modifier.testTag("inventory_search_field")
                )
            }

            // Status Filter Chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // All items
                item {
                    val isAllSelected = uiState.selectedStatus == null && uiState.selectedOwner == null && uiState.selectedCategory == null
                    FilterChip(
                        selected = isAllSelected,
                        onClick = {
                            viewModel.onStatusFilterChanged(null)
                            viewModel.onOwnerFilterChanged(null)
                            viewModel.onCategoryFilterChanged(null)
                        },
                        label = {
                            Text(
                                text = "All (${uiState.totalCount})",
                                fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        border = BorderStroke(1.dp, if (isAllSelected) colors.forest else colors.cardBorder),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = colors.forest,
                            selectedLabelColor = Color.White,
                            containerColor = colors.cardCream,
                            labelColor = colors.ink
                        )
                    )
                }

                // Low Stock
                item {
                    val isLowSelected = uiState.selectedStatus == StockStatus.LOW_STOCK
                    FilterChip(
                        selected = isLowSelected,
                        onClick = {
                            viewModel.onStatusFilterChanged(
                                if (isLowSelected) null else StockStatus.LOW_STOCK
                            )
                        },
                        label = {
                            Text(
                                text = "Low Stock (${uiState.lowStockCount})",
                                fontWeight = if (isLowSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        border = BorderStroke(1.dp, if (isLowSelected) Amber else colors.cardBorder),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Amber,
                            selectedLabelColor = Color.White,
                            containerColor = if (colors.isDark) colors.sageSoft else AmberLight,
                            labelColor = if (colors.isDark) AmberLight else Amber
                        )
                    )
                }

                // Out of Stock
                item {
                    val isOutSelected = uiState.selectedStatus == StockStatus.OUT_OF_STOCK
                    FilterChip(
                        selected = isOutSelected,
                        onClick = {
                            viewModel.onStatusFilterChanged(
                                if (isOutSelected) null else StockStatus.OUT_OF_STOCK
                            )
                        },
                        label = {
                            Text(
                                text = "Out of Stock (${uiState.outOfStockCount})",
                                fontWeight = if (isOutSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        border = BorderStroke(1.dp, if (isOutSelected) AlertRed else colors.cardBorder),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AlertRed,
                            selectedLabelColor = Color.White,
                            containerColor = if (colors.isDark) colors.sageSoft else AlertRedLight,
                            labelColor = if (colors.isDark) AlertRedLight else AlertRed
                        )
                    )
                }

                // In Stock
                item {
                    val isInSelected = uiState.selectedStatus == StockStatus.IN_STOCK
                    FilterChip(
                        selected = isInSelected,
                        onClick = {
                            viewModel.onStatusFilterChanged(
                                if (isInSelected) null else StockStatus.IN_STOCK
                            )
                        },
                        label = {
                            Text(
                                text = "In Stock (${uiState.inStockCount})",
                                fontWeight = if (isInSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        border = BorderStroke(1.dp, if (isInSelected) colors.success else colors.cardBorder),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = colors.success,
                            selectedLabelColor = Color.White,
                            containerColor = colors.cardCream,
                            labelColor = colors.ink
                        )
                    )
                }

                // Owners Filter Chips
                items(uiState.availableOwners) { ownerName ->
                    val isOwnerSelected = uiState.selectedOwner.equals(ownerName, ignoreCase = true)
                    FilterChip(
                        selected = isOwnerSelected,
                        onClick = {
                            viewModel.onOwnerFilterChanged(if (isOwnerSelected) null else ownerName)
                        },
                        label = {
                            Text(
                                text = ownerName,
                                fontWeight = if (isOwnerSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        border = BorderStroke(1.dp, if (isOwnerSelected) colors.forest else colors.cardBorder),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = colors.forest,
                            selectedLabelColor = Color.White,
                            containerColor = colors.cardCream,
                            labelColor = colors.ink
                        )
                    )
                }
            }

            // Products List
            if (uiState.filteredProducts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyStateView(
                        icon = Icons.Default.SearchOff,
                        title = "No products found",
                        description = if (uiState.searchQuery.isNotBlank()) "No items match '${uiState.searchQuery}'" else "Try clearing filters or add a new product.",
                        actionButtonText = "Clear Filters",
                        onActionClick = {
                            viewModel.onSearchQueryChanged("")
                            viewModel.onStatusFilterChanged(null)
                            viewModel.onOwnerFilterChanged(null)
                            viewModel.onCategoryFilterChanged(null)
                        }
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(uiState.filteredProducts, key = { it.id }) { product ->
                        ProductItemCard(
                            product = product,
                            onAdjustStock = { viewModel.openStockAdjustment(product) },
                            onEdit = { viewModel.openProductForm(product) },
                            onDelete = { productToDelete = product },
                            onViewQr = { onNavigateToQrLabel(product) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProductItemCard(
    product: Product,
    onAdjustStock: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onViewQr: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalTindaColors.current

    TindaCard(
        modifier = modifier.fillMaxWidth(),
        backgroundColor = colors.cardCream,
        borderColor = colors.cardBorder,
        elevation = 2.dp
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Row 1: Emoji Avatar + Title + QR code + Status badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(colors.sageSoft),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = product.photoPath ?: "📦",
                            fontSize = 24.sp
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(
                            text = product.name,
                            style = TindaTypography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = colors.ink,
                            maxLines = 2
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = product.qrCode,
                                fontSize = 11.sp,
                                fontFamily = DmSansFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = colors.forest
                            )
                            Text(text = "•", fontSize = 10.sp, color = colors.muted)
                            Text(
                                text = product.category,
                                fontSize = 11.sp,
                                color = colors.muted
                            )
                            Text(text = "•", fontSize = 10.sp, color = colors.muted)
                            Text(
                                text = product.owner,
                                fontSize = 11.sp,
                                color = colors.forestSoft,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                StockStatusBadge(status = product.status)
            }

            HorizontalDivider(color = colors.cardBorder, thickness = 0.8.dp)

            // Row 2: Quantities, Selling Price, and Profit Margin + Quick Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column {
                        Text(text = "Stock", fontSize = 11.sp, color = colors.muted)
                        Text(
                            text = "${product.quantity} pcs",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (product.status) {
                                StockStatus.OUT_OF_STOCK -> AlertRed
                                StockStatus.LOW_STOCK -> Amber
                                StockStatus.IN_STOCK -> colors.forest
                            }
                        )
                    }

                    Column {
                        Text(text = "Selling Price", fontSize = 11.sp, color = colors.muted)
                        CurrencyText(
                            centavos = product.sellingPrice,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.ink
                        )
                    }

                    Column {
                        Text(text = "Margin", fontSize = 11.sp, color = colors.muted)
                        Text(
                            text = "+${CurrencyHelper.formatCentavos(product.profitMarginCentavos)}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.success
                        )
                    }
                }

                // Row of quick action circular buttons
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // QR label button
                    IconButton(
                        onClick = onViewQr,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(colors.sageSoft)
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCode,
                            contentDescription = "QR Code Label",
                            tint = colors.forest,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Stock In / Out / Adjust
                    IconButton(
                        onClick = onAdjustStock,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(colors.sageSoft)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapVert,
                            contentDescription = "Adjust Stock",
                            tint = colors.forest,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Edit product
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(colors.sageSoft)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Product",
                            tint = colors.forest,
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    // Delete product
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(if (colors.isDark) Color(0xFF381E1C) else AlertRedLight)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete Product",
                            tint = AlertRed,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
