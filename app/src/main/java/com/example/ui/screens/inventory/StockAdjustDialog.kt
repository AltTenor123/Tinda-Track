package com.example.ui.screens.inventory

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.domain.model.Product
import com.example.domain.model.StockMovementType
import com.example.ui.components.StockStatusBadge
import com.example.ui.components.TindaPrimaryButton
import com.example.ui.components.TindaSecondaryButton
import com.example.ui.theme.*

@Composable
fun StockAdjustDialog(
    product: Product,
    onDismiss: () -> Unit,
    onConfirm: (type: StockMovementType, count: Int, reason: String) -> Unit
) {
    val colors = LocalTindaColors.current

    var selectedType by remember { mutableStateOf(StockMovementType.IN) }
    var quantityText by remember { mutableStateOf("10") }
    var reason by remember { mutableStateOf("Supplier restock delivery") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val quickReasons = when (selectedType) {
        StockMovementType.IN -> listOf("Supplier delivery", "Wholesale restock", "Found extra units", "Return from customer")
        StockMovementType.OUT -> listOf("Expired / spoiled", "Damaged packaging", "Owner personal consumption", "Pest damage")
        StockMovementType.ADJUST -> listOf("Physical inventory audit", "Count discrepancy correction", "Store re-organization")
        else -> emptyList()
    }

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = colors.cardCream,
        unfocusedContainerColor = colors.cardCream,
        focusedBorderColor = colors.forest,
        unfocusedBorderColor = colors.cardBorder,
        focusedTextColor = colors.ink,
        unfocusedTextColor = colors.ink,
        focusedLabelColor = colors.forest,
        unfocusedLabelColor = colors.muted
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = colors.cardCream),
            border = BorderStroke(1.dp, colors.cardBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Stock Movement",
                            style = TindaTypography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = colors.ink
                        )
                        Text(
                            text = product.name,
                            style = TindaTypography.bodySmall,
                            color = colors.muted
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(colors.sageSoft)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = colors.ink,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Current stock pill
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(colors.sageSoft)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Current In Stock", fontSize = 11.sp, color = colors.muted)
                        Text(
                            text = "${product.quantity} pcs",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.forest
                        )
                    }
                    StockStatusBadge(status = product.status)
                }

                // Segmented Tabs: IN, OUT, ADJUST
                TabRow(
                    selectedTabIndex = when (selectedType) {
                        StockMovementType.IN -> 0
                        StockMovementType.OUT -> 1
                        StockMovementType.ADJUST -> 2
                        else -> 0
                    },
                    containerColor = colors.sageSoft,
                    contentColor = colors.forest,
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = selectedType == StockMovementType.IN,
                        onClick = {
                            selectedType = StockMovementType.IN
                            reason = "Supplier restock delivery"
                            errorMessage = null
                        },
                        text = {
                            Text(
                                "Stock In (+)",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = if (selectedType == StockMovementType.IN) colors.forest else colors.muted
                            )
                        }
                    )
                    Tab(
                        selected = selectedType == StockMovementType.OUT,
                        onClick = {
                            selectedType = StockMovementType.OUT
                            reason = "Damaged packaging"
                            errorMessage = null
                        },
                        text = {
                            Text(
                                "Stock Out (-)",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = if (selectedType == StockMovementType.OUT) AlertRed else colors.muted
                            )
                        }
                    )
                    Tab(
                        selected = selectedType == StockMovementType.ADJUST,
                        onClick = {
                            selectedType = StockMovementType.ADJUST
                            quantityText = product.quantity.toString()
                            reason = "Physical inventory audit"
                            errorMessage = null
                        },
                        text = {
                            Text(
                                "Set Count (=)",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = if (selectedType == StockMovementType.ADJUST) colors.forest else colors.muted
                            )
                        }
                    )
                }

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = AlertRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Count input
                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { quantityText = it; errorMessage = null },
                    label = {
                        Text(
                            when (selectedType) {
                                StockMovementType.IN -> "Quantity to Add (+)"
                                StockMovementType.OUT -> "Quantity to Deduct (-)"
                                StockMovementType.ADJUST -> "Exact Physical Count (=)"
                                else -> "Quantity"
                            }
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_adjust_quantity"),
                    shape = RoundedCornerShape(16.dp),
                    colors = textFieldColors,
                    singleLine = true
                )

                // Quick reason chips
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = "Reason (Required)", style = TindaTypography.titleSmall, color = colors.ink)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        quickReasons.take(2).forEach { r ->
                            SuggestionChip(
                                onClick = { reason = r },
                                label = { Text(r, fontSize = 11.sp, color = colors.ink) },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = colors.sageSoft
                                )
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it; errorMessage = null },
                    label = { Text("Reason Details *") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_adjust_reason"),
                    shape = RoundedCornerShape(16.dp),
                    colors = textFieldColors,
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TindaSecondaryButton(
                        text = "Cancel",
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    )

                    TindaPrimaryButton(
                        text = "Confirm",
                        onClick = {
                            val count = quantityText.toIntOrNull()
                            if (count == null || (selectedType != StockMovementType.ADJUST && count <= 0)) {
                                errorMessage = "Enter a valid positive number!"
                                return@TindaPrimaryButton
                            }
                            if (selectedType == StockMovementType.ADJUST && count < 0) {
                                errorMessage = "Count cannot be negative!"
                                return@TindaPrimaryButton
                            }
                            if (selectedType == StockMovementType.OUT && count > product.quantity) {
                                errorMessage = "Cannot deduct $count. Only ${product.quantity} in stock!"
                                return@TindaPrimaryButton
                            }
                            if (reason.isBlank()) {
                                errorMessage = "Reason is required for inventory audit!"
                                return@TindaPrimaryButton
                            }

                            onConfirm(selectedType, count, reason.trim())
                        },
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("confirm_adjust_button")
                    )
                }
            }
        }
    }
}
