package com.example.ui.screens.scan

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.model.CartManager
import com.example.domain.model.CurrencyHelper
import com.example.domain.model.Product
import com.example.domain.model.StockStatus
import com.example.platform.CameraScannerView
import com.example.ui.components.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen(
    viewModel: ScanViewModel,
    cartManager: CartManager,
    onNavigateToCheckout: () -> Unit,
    onAddNewProductWithCode: (String) -> Unit,
    onAdjustStock: (Product) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val cartItems by cartManager.items.collectAsStateWithLifecycle()
    val cartCount = cartManager.itemCount
    val cartTotal = cartManager.totalCentavos

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
            .testTag("scan_screen"),
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
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Scan QR or Barcode",
                        style = TindaTypography.titleMedium,
                        color = Ink
                    )

                    // Lookup vs Sell Mode Toggle
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Cream)
                            .border(1.dp, CardBorder, RoundedCornerShape(20.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        ModeTogglePill(
                            title = "🔍 Lookup Item",
                            isSelected = uiState.mode == ScanMode.LOOKUP,
                            onClick = { viewModel.setMode(ScanMode.LOOKUP) }
                        )

                        ModeTogglePill(
                            title = "🛒 Quick Sell",
                            isSelected = uiState.mode == ScanMode.SELL,
                            onClick = { viewModel.setMode(ScanMode.SELL) }
                        )
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
            // Live Camera Viewfinder
            CameraScannerView(
                onCodeScanned = { code ->
                    viewModel.onCodeDetected(code)
                },
                modifier = Modifier.fillMaxSize()
            )

            // Top Status Info Pill
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Forest.copy(alpha = 0.85f))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = if (uiState.mode == ScanMode.LOOKUP) {
                        "Point at code to view details & stock"
                    } else {
                        "Point at code to instantly add 1 to cart"
                    },
                    style = TindaTypography.bodySmall,
                    color = CreamLight,
                    fontWeight = FontWeight.Medium
                )
            }

            // Bottom Section: Manual Entry & Floating Cart Button
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Floating Cart Quick Button (shown if cart has items)
                AnimatedVisibility(
                    visible = cartCount > 0,
                    enter = slideInVertically { it } + fadeIn(),
                    exit = slideOutVertically { it } + fadeOut()
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onNavigateToCheckout)
                            .testTag("floating_cart_button"),
                        shape = RoundedCornerShape(20.dp),
                        color = Forest,
                        shadowElevation = 8.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(AccentGold),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$cartCount",
                                        style = TindaTypography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Forest
                                    )
                                }
                                Column {
                                    Text(
                                        text = "Current Cart",
                                        style = TindaTypography.labelSmall,
                                        color = Sage
                                    )
                                    Text(
                                        text = CurrencyHelper.formatCentavos(cartTotal),
                                        style = TindaTypography.titleSmall,
                                        color = CreamLight,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Checkout",
                                    style = TindaTypography.labelMedium,
                                    color = CreamLight,
                                    fontWeight = FontWeight.Bold
                                )
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = CreamLight,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                // Manual Code Entry Card
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = CardCream.copy(alpha = 0.95f),
                    border = BorderStroke(1.dp, CardBorder),
                    shadowElevation = 3.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextField(
                            value = uiState.manualInputText,
                            onValueChange = { viewModel.onManualInputChanged(it) },
                            placeholder = {
                                Text("Enter code manually (e.g. TT-000001)", style = TindaTypography.bodySmall)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("manual_code_input"),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = { viewModel.submitManualEntry() }),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )

                        IconButton(
                            onClick = { viewModel.submitManualEntry() },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Forest)
                                .testTag("manual_code_submit")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Lookup Code",
                                tint = CreamLight,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal Bottom Sheet: Lookup Product Info
    if (uiState.isProductSheetOpen && uiState.activeProduct != null) {
        val product = uiState.activeProduct!!
        ModalBottomSheet(
            onDismissRequest = { viewModel.closeProductSheet() },
            containerColor = CardCream,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Product Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = product.name,
                            style = TindaTypography.titleLarge,
                            color = Ink
                        )
                        Text(
                            text = "${product.category} • Owner: ${product.owner}",
                            style = TindaTypography.bodySmall,
                            color = Muted
                        )
                    }
                    StockStatusBadge(status = product.status)
                }

                HorizontalDivider(color = CardBorder, thickness = 1.dp)

                // Key Info Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "Selling Price", style = TindaTypography.labelSmall, color = Muted)
                        Text(
                            text = CurrencyHelper.formatCentavos(product.sellingPrice),
                            style = TindaTypography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Forest
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Current Stock", style = TindaTypography.labelSmall, color = Muted)
                        Text(
                            text = "${product.quantity} pcs",
                            style = TindaTypography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (product.quantity <= product.reorderLevel) AlertRed else Ink
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "Profit Margin", style = TindaTypography.labelSmall, color = Muted)
                        Text(
                            text = "+${CurrencyHelper.formatCentavos(product.sellingPrice - product.costPrice)}",
                            style = TindaTypography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = SageDeep
                        )
                    }
                }

                // Codes info
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Cream,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "QR: ${product.qrCode}", style = TindaTypography.bodySmall, color = Ink)
                        Text(text = "SKU: ${product.sku}", style = TindaTypography.bodySmall, color = Muted)
                    }
                }

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TindaOutlinedButton(
                        text = "Adjust Stock",
                        icon = Icons.Default.SwapVert,
                        onClick = {
                            viewModel.closeProductSheet()
                            onAdjustStock(product)
                        },
                        modifier = Modifier.weight(1f)
                    )

                    TindaPrimaryButton(
                        text = "Add to Cart",
                        icon = Icons.Default.AddShoppingCart,
                        enabled = product.quantity > 0,
                        onClick = {
                            viewModel.addActiveProductToCart(1)
                        },
                        modifier = Modifier.weight(1.3f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }

    // Modal Bottom Sheet: Unknown Code Scanned
    if (uiState.isUnknownCodeSheetOpen && uiState.unknownCodeScanned != null) {
        val code = uiState.unknownCodeScanned!!
        ModalBottomSheet(
            onDismissRequest = { viewModel.closeUnknownCodeSheet() },
            containerColor = CardCream,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Cream),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                        contentDescription = null,
                        tint = AccentGold,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Text(
                    text = "Product Not Found",
                    style = TindaTypography.titleMedium,
                    color = Ink
                )

                Text(
                    text = "Scanned code \"$code\" is not associated with any product in your inventory.",
                    style = TindaTypography.bodyMedium,
                    color = Muted,
                    textAlign = TextAlign.Center
                )

                TindaPrimaryButton(
                    text = "Register as New Product",
                    icon = Icons.Default.Add,
                    onClick = {
                        viewModel.closeUnknownCodeSheet()
                        onAddNewProductWithCode(code)
                    }
                )

                TindaOutlinedButton(
                    text = "Dismiss",
                    onClick = { viewModel.closeUnknownCodeSheet() }
                )

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun ModeTogglePill(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) Forest else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            style = TindaTypography.labelMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) CreamLight else Muted
        )
    }
}
