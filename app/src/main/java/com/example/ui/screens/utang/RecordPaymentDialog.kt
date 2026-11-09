package com.example.ui.screens.utang

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
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
import com.example.domain.model.CurrencyHelper
import com.example.domain.model.Utang
import com.example.domain.model.UtangPayment
import com.example.ui.components.TindaOutlinedButton
import com.example.ui.components.TindaPrimaryButton
import com.example.ui.theme.*

@Composable
fun RecordPaymentDialog(
    utang: Utang,
    payments: List<UtangPayment>,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onConfirmPayment: (paymentCentavos: Long) -> Unit
) {
    var amountInput by remember { mutableStateOf("") }
    val paymentCentavos = CurrencyHelper.parseToCentavos(amountInput)
    val remainingCentavos = utang.balance

    val isOverpaying = paymentCentavos > remainingCentavos
    val isValid = paymentCentavos > 0 && !isOverpaying

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(12.dp)
                .testTag("record_payment_dialog"),
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
                            text = "Record Payment (Bayad)",
                            style = TindaTypography.titleMedium,
                            color = Ink
                        )
                        Text(
                            text = "Customer: ${utang.customerName}",
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

                // Balance Summary Card
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Cream,
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
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
                                Text(text = "Paid So Far", style = TindaTypography.labelSmall, color = Muted)
                                Text(
                                    text = CurrencyHelper.formatCentavos(utang.paid),
                                    style = TindaTypography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = SageDeep
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(text = "Remaining", style = TindaTypography.labelSmall, color = Muted)
                                Text(
                                    text = CurrencyHelper.formatCentavos(remainingCentavos),
                                    style = TindaTypography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (remainingCentavos > 0) Amber else SuccessGreen
                                )
                            }
                        }

                        if (utang.itemsNote.isNotBlank()) {
                            HorizontalDivider(color = CardBorder, thickness = 1.dp)
                            Text(
                                text = "Items: ${utang.itemsNote}",
                                style = TindaTypography.bodySmall,
                                color = Muted
                            )
                        }
                    }
                }

                // Error Message if any
                if (errorMessage != null || isOverpaying) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = AlertRedLight,
                        border = androidx.compose.foundation.BorderStroke(1.dp, AlertRedBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = AlertRed,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = errorMessage ?: "Payment cannot exceed remaining balance of ${CurrencyHelper.formatCentavos(remainingCentavos)}!",
                                style = TindaTypography.bodySmall,
                                color = AlertRed
                            )
                        }
                    }
                }

                // Payment input
                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { amountInput = it },
                    label = { Text("Payment Amount (₱) *") },
                    placeholder = { Text("0.00") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("utang_payment_input"),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Forest,
                        unfocusedBorderColor = CardBorder,
                        focusedLabelColor = Forest
                    )
                )

                // Quick presets
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PresetChip(
                        label = "Pay in Full",
                        onClick = { amountInput = (remainingCentavos / 100.0).toString() },
                        modifier = Modifier.weight(1.2f)
                    )
                    PresetChip(
                        label = "₱50",
                        onClick = { amountInput = "50" },
                        modifier = Modifier.weight(1f)
                    )
                    PresetChip(
                        label = "₱100",
                        onClick = { amountInput = "100" },
                        modifier = Modifier.weight(1f)
                    )
                    PresetChip(
                        label = "₱200",
                        onClick = { amountInput = "200" },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Payment History List
                Text(
                    text = "Payment History (${payments.size})",
                    style = TindaTypography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Ink
                )

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .border(1.dp, CardBorder, RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White
                ) {
                    if (payments.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No payments recorded yet",
                                style = TindaTypography.bodySmall,
                                color = Muted
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(payments) { p ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = p.createdAt.take(16).replace("T", " "),
                                        style = TindaTypography.bodySmall,
                                        color = Muted
                                    )
                                    Text(
                                        text = "+${CurrencyHelper.formatCentavos(p.amount)}",
                                        style = TindaTypography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Forest
                                    )
                                }
                            }
                        }
                    }
                }

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TindaOutlinedButton(
                        text = "Cancel",
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    )

                    TindaPrimaryButton(
                        text = "Confirm Bayad",
                        enabled = isValid,
                        onClick = { onConfirmPayment(paymentCentavos) },
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("submit_payment_button")
                    )
                }
            }
        }
    }
}

@Composable
private fun PresetChip(
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
