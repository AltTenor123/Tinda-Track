package com.example.ui.screens.utang

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.domain.model.CurrencyHelper
import com.example.ui.components.TindaOutlinedButton
import com.example.ui.components.TindaPrimaryButton
import com.example.ui.theme.*

@Composable
fun AddUtangDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, itemsNote: String, amountCentavos: Long) -> Unit
) {
    var customerName by remember { mutableStateOf("") }
    var itemsNote by remember { mutableStateOf("") }
    var amountInput by remember { mutableStateOf("") }

    val amountCentavos = CurrencyHelper.parseToCentavos(amountInput)
    val isValid = customerName.isNotBlank() && amountCentavos > 0

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("add_utang_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = CardCream,
            tonalElevation = 6.dp
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
                            text = "New Utang Record",
                            style = TindaTypography.titleMedium,
                            color = Ink
                        )
                        Text(
                            text = "Customer store credit ledger",
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

                // Customer Name
                OutlinedTextField(
                    value = customerName,
                    onValueChange = { customerName = it },
                    label = { Text("Customer Name *") },
                    placeholder = { Text("e.g. Aling Marites, Mang Tomas") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("utang_customer_name_input"),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Forest,
                        unfocusedBorderColor = CardBorder,
                        focusedLabelColor = Forest
                    )
                )

                // Items / Note
                OutlinedTextField(
                    value = itemsNote,
                    onValueChange = { itemsNote = it },
                    label = { Text("Items / Description") },
                    placeholder = { Text("e.g. 2x Bigas, 1x Mantika, 3x Kape") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("utang_items_note_input"),
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Forest,
                        unfocusedBorderColor = CardBorder,
                        focusedLabelColor = Forest
                    )
                )

                // Total Amount
                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { amountInput = it },
                    label = { Text("Total Utang Amount (₱) *") },
                    placeholder = { Text("0.00") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("utang_amount_input"),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Forest,
                        unfocusedBorderColor = CardBorder,
                        focusedLabelColor = Forest
                    )
                )

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
                        text = "Record Utang",
                        enabled = isValid,
                        onClick = {
                            onConfirm(customerName, itemsNote, amountCentavos)
                        },
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("submit_utang_button")
                    )
                }
            }
        }
    }
}
