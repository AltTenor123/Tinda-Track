package com.example.ui.screens.sales

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.domain.model.CurrencyHelper
import com.example.domain.model.Sale
import com.example.ui.components.TindaOutlinedButton
import com.example.ui.components.TindaPrimaryButton
import com.example.ui.theme.*

@Composable
fun ReceiptDialog(
    sale: Sale,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(12.dp)
                .testTag("receipt_dialog"),
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Forest,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Sales Receipt",
                            style = TindaTypography.titleMedium,
                            color = Ink
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

                // Paper Receipt Canvas
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .border(1.dp, CardBorder, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Header
                        item {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = "TINDA TRACK SARI-SARI STORE",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = Ink,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "Barangay San Antonio, Pasig City",
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = Muted
                                )
                                Text(
                                    text = "Receipt: ${sale.receiptNo}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = Forest
                                )
                                Text(
                                    text = "Date: ${sale.createdAt} • Cashier: ${sale.cashier}",
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = Muted
                                )
                                Divider(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    color = SageSoft,
                                    thickness = 1.dp
                                )
                            }
                        }

                        // Items list
                        items(sale.items) { item ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.productName,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        fontFamily = FontFamily.Monospace,
                                        color = Ink
                                    )
                                    Text(
                                        text = "${item.quantity} x ${CurrencyHelper.formatCentavos(item.unitPrice)}",
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = Muted
                                    )
                                }
                                Text(
                                    text = CurrencyHelper.formatCentavos(item.subtotal),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = Ink
                                )
                            }
                        }

                        // Totals summary
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Divider(color = SageSoft, thickness = 1.dp)

                                ReceiptTotalLine(
                                    label = "TOTAL AMOUNT:",
                                    value = CurrencyHelper.formatCentavos(sale.total),
                                    isBold = true,
                                    fontSize = 15.sp,
                                    valueColor = Forest
                                )

                                ReceiptTotalLine(
                                    label = "Cash Tendered:",
                                    value = CurrencyHelper.formatCentavos(sale.cashReceived),
                                    isBold = false,
                                    fontSize = 13.sp
                                )

                                ReceiptTotalLine(
                                    label = "Change Due:",
                                    value = CurrencyHelper.formatCentavos(sale.change),
                                    isBold = true,
                                    fontSize = 14.sp,
                                    valueColor = if (sale.change > 0) SageDeep else Ink
                                )

                                Divider(
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    color = SageSoft,
                                    thickness = 1.dp
                                )

                                // Estimated Profit badge (store owner internal info)
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = SageSoft,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Estimated Profit Margin",
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = Forest
                                        )
                                        Text(
                                            text = "+${CurrencyHelper.formatCentavos(sale.estimatedProfit)}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace,
                                            color = Forest
                                        )
                                    }
                                }

                                Text(
                                    text = "Maraming Salamat! Balik po kayo muli!",
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = Muted,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 10.dp)
                                )
                            }
                        }
                    }
                }

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TindaOutlinedButton(
                        text = "Close",
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    )

                    TindaPrimaryButton(
                        text = "Share Receipt",
                        icon = Icons.Default.Share,
                        onClick = {
                            shareReceiptAsText(context, sale)
                        },
                        modifier = Modifier.weight(1.3f)
                    )
                }
            }
        }
    }
}

@Composable
private fun ReceiptTotalLine(
    label: String,
    value: String,
    isBold: Boolean,
    fontSize: androidx.compose.ui.unit.TextUnit,
    valueColor: Color = Ink
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = fontSize,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            fontFamily = FontFamily.Monospace,
            color = Ink
        )
        Text(
            text = value,
            fontSize = fontSize,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            fontFamily = FontFamily.Monospace,
            color = valueColor
        )
    }
}

private fun shareReceiptAsText(context: Context, sale: Sale) {
    val itemsSummary = sale.items.joinToString("\n") {
        "- ${it.productName}: ${it.quantity}x @ ${CurrencyHelper.formatCentavos(it.unitPrice)} = ${CurrencyHelper.formatCentavos(it.subtotal)}"
    }

    val receiptBody = """
        ========================================
                 TINDA TRACK STORE RECEIPT
        ========================================
        Receipt No: ${sale.receiptNo}
        Date: ${sale.createdAt}
        Cashier: ${sale.cashier}
        ----------------------------------------
        ITEMS:
        $itemsSummary
        ----------------------------------------
        TOTAL: ${CurrencyHelper.formatCentavos(sale.total)}
        CASH TENDERED: ${CurrencyHelper.formatCentavos(sale.cashReceived)}
        CHANGE: ${CurrencyHelper.formatCentavos(sale.change)}
        ESTIMATED PROFIT: +${CurrencyHelper.formatCentavos(sale.estimatedProfit)}
        ========================================
             Maraming Salamat! Balik po kayo!
    """.trimIndent()

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Receipt: ${sale.receiptNo} (${CurrencyHelper.formatCentavos(sale.total)})")
        putExtra(Intent.EXTRA_TEXT, receiptBody)
    }
    context.startActivity(Intent.createChooser(intent, "Share Sales Receipt"))
}
