package com.example.ui.screens.sales

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.model.CartItem
import com.example.domain.model.CartManager
import com.example.domain.model.CurrencyHelper
import com.example.ui.components.TindaOutlinedButton
import com.example.ui.components.TindaPrimaryButton
import com.example.ui.theme.*

@Composable
fun CartCheckoutDialog(
    cartManager: CartManager,
    onCheckout: (cashReceivedCentavos: Long) -> Unit,
    onDismiss: () -> Unit,
    errorMessage: String? = null,
    isCheckingOut: Boolean = false
) {
    val cartItems by cartManager.items.collectAsStateWithLifecycle()
    val totalCentavos = cartManager.totalCentavos
    val profitCentavos = cartManager.estimatedProfitCentavos

    var cashInput by remember { mutableStateOf("") }
    val cashReceivedCentavos = CurrencyHelper.parseToCentavos(cashInput)
    val changeCentavos = (cashReceivedCentavos - totalCentavos).coerceAtLeast(0L)
    val isPaymentSufficient = cashReceivedCentavos >= totalCentavos && totalCentavos > 0

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .padding(12.dp)
                .testTag("cart_checkout_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = CardCream,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Checkout & Payment",
                            style = TindaTypography.titleMedium,
                            color = Ink
                        )
                        Text(
                            text = "${cartManager.itemCount} items in cart",
                            style = TindaTypography.bodySmall,
                            color = Muted
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Muted
                        )
                    }
                }

                // Error message banner if any
                if (errorMessage != null) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = AlertRedLight,
                        border = androidx.compose.foundation.BorderStroke(1.dp, AlertRedBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = AlertRed,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = errorMessage,
                                style = TindaTypography.bodySmall,
                                color = AlertRed,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Cart Items List
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .border(1.dp, CardBorder, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White
                ) {
                    if (cartItems.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Cart is empty. Add products from Inventory or Scan tab.",
                                style = TindaTypography.bodyMedium,
                                color = Muted,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(24.dp)
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(cartItems, key = { it.product.id }) { item ->
                                CartItemRow(
                                    item = item,
                                    onQuantityChanged = { newQty ->
                                        cartManager.updateQuantity(item.product.id, newQty)
                                    },
                                    onRemove = {
                                        cartManager.removeItem(item.product.id)
                                    }
                                )
                            }
                        }
                    }
                }

                // Payment Section
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Cream,
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Total and Profit
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = "Total to Pay", style = TindaTypography.labelSmall, color = Muted)
                                Text(
                                    text = CurrencyHelper.formatCentavos(totalCentavos),
                                    style = TindaTypography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Forest
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = SageSoft
                            ) {
                                Text(
                                    text = "Est. Profit: +${CurrencyHelper.formatCentavos(profitCentavos)}",
                                    style = TindaTypography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Forest,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        // Cash input
                        OutlinedTextField(
                            value = cashInput,
                            onValueChange = { cashInput = it },
                            label = { Text("Cash Received (₱)") },
                            placeholder = { Text("0.00") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("cash_received_input"),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Forest,
                                unfocusedBorderColor = CardBorder,
                                focusedLabelColor = Forest
                            )
                        )

                        // Quick cash presets
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            QuickCashChip(
                                label = "Exact",
                                onClick = {
                                    cashInput = (totalCentavos / 100.0).toString()
                                },
                                modifier = Modifier.weight(1f)
                            )
                            QuickCashChip(
                                label = "₱50",
                                onClick = { cashInput = "50" },
                                modifier = Modifier.weight(1f)
                            )
                            QuickCashChip(
                                label = "₱100",
                                onClick = { cashInput = "100" },
                                modifier = Modifier.weight(1f)
                            )
                            QuickCashChip(
                                label = "₱500",
                                onClick = { cashInput = "500" },
                                modifier = Modifier.weight(1f)
                            )
                            QuickCashChip(
                                label = "₱1000",
                                onClick = { cashInput = "1000" },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Change Due
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Change Due:",
                                style = TindaTypography.titleSmall,
                                color = Ink
                            )
                            Text(
                                text = CurrencyHelper.formatCentavos(changeCentavos),
                                style = TindaTypography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isPaymentSufficient) SageDeep else AlertRed
                            )
                        }
                    }
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TindaOutlinedButton(
                        text = "Cancel",
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    )

                    TindaPrimaryButton(
                        text = if (isCheckingOut) "Recording..." else "Complete Sale",
                        icon = Icons.Default.Check,
                        enabled = isPaymentSufficient && !isCheckingOut && cartItems.isNotEmpty(),
                        onClick = {
                            onCheckout(cashReceivedCentavos)
                        },
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("confirm_checkout_button")
                    )
                }
            }
        }
    }
}

@Composable
private fun CartItemRow(
    item: CartItem,
    onQuantityChanged: (Int) -> Unit,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
            .background(CreamLight, RoundedCornerShape(12.dp))
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.product.name,
                style = TindaTypography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = Ink,
                maxLines = 1
            )
            Text(
                text = "${CurrencyHelper.formatCentavos(item.product.sellingPrice)} each • Stock: ${item.product.quantity}",
                style = TindaTypography.bodySmall,
                color = Muted
            )
            Text(
                text = "Subtotal: ${CurrencyHelper.formatCentavos(item.subtotal)}",
                style = TindaTypography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = Forest
            )
        }

        // Stepper: Minus, Qty, Plus
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            IconButton(
                onClick = {
                    if (item.quantity > 1) {
                        onQuantityChanged(item.quantity - 1)
                    } else {
                        onRemove()
                    }
                },
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Cream)
            ) {
                Icon(
                    imageVector = if (item.quantity == 1) Icons.Default.Delete else Icons.Default.Remove,
                    contentDescription = "Decrease",
                    tint = if (item.quantity == 1) AlertRed else Ink,
                    modifier = Modifier.size(16.dp)
                )
            }

            Text(
                text = "${item.quantity}",
                style = TindaTypography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Ink,
                modifier = Modifier.widthIn(min = 24.dp),
                textAlign = TextAlign.Center
            )

            IconButton(
                onClick = {
                    if (item.quantity < item.product.quantity) {
                        onQuantityChanged(item.quantity + 1)
                    }
                },
                enabled = item.quantity < item.product.quantity,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(if (item.quantity < item.product.quantity) Forest else SageSoft)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Increase",
                    tint = if (item.quantity < item.product.quantity) CreamLight else Muted,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun QuickCashChip(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = CardCream,
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Text(
            text = label,
            style = TindaTypography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = Forest,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 6.dp)
        )
    }
}
