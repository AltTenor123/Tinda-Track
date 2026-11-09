package com.example.ui.screens.inventory

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.domain.model.CurrencyHelper
import com.example.domain.model.Product
import com.example.ui.components.TindaPrimaryButton
import com.example.ui.components.TindaSecondaryButton
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductFormSheet(
    product: Product?,
    availableCategories: List<String>,
    availableOwners: List<String>,
    onDismiss: () -> Unit,
    onSave: (
        id: Long,
        name: String,
        owner: String,
        category: String,
        supplier: String,
        sku: String,
        qrCode: String,
        costPrice: Long,
        sellingPrice: Long,
        quantity: Int,
        reorderLevel: Int,
        expiryDate: String?,
        emoji: String?
    ) -> Unit
) {
    val isEditing = product != null
    val colors = LocalTindaColors.current

    var name by remember { mutableStateOf(product?.name ?: "") }
    var owner by remember { mutableStateOf(product?.owner ?: "Ate Susan") }
    var category by remember { mutableStateOf(product?.category ?: "Instant Noodles") }
    var supplier by remember { mutableStateOf(product?.supplier ?: "Puregold Wholesale") }
    var sku by remember { mutableStateOf(product?.sku ?: "") }
    var qrCode by remember { mutableStateOf(product?.qrCode ?: "") }
    var costPriceText by remember {
        mutableStateOf(if (product != null) "%.2f".format(product.costPrice / 100.0) else "12.00")
    }
    var sellingPriceText by remember {
        mutableStateOf(if (product != null) "%.2f".format(product.sellingPrice / 100.0) else "15.00")
    }
    var quantityText by remember { mutableStateOf(product?.quantity?.toString() ?: "20") }
    var reorderLevelText by remember { mutableStateOf(product?.reorderLevel?.toString() ?: "5") }
    var expiryDate by remember { mutableStateOf(product?.expiryDate ?: "") }
    var selectedEmoji by remember { mutableStateOf(product?.photoPath ?: "🍜") }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    val defaultOwners = listOf("Ate Susan", "Nanay Cora", "Kuya Bert")
    val defaultCategories = listOf(
        "Instant Noodles", "Beverages", "Snacks & Chips", "Canned Goods",
        "Personal Care", "Coffee & Tea", "Condiments & Cooking",
        "Laundry & Cleaning", "Biscuits & Bakery", "Dairy", "Candies & Sweets"
    )
    val defaultEmojis = listOf("🍜", "🥤", "☕", "🧼", "🧴", "🥫", "🥔", "🍘", "🍾", "🧂", "🫧", "🥛", "🍬", "🍫", "🍞", "🥚", "📦")

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

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            color = colors.cream
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isEditing) "Edit Product" else "Add New Paninda",
                            style = TindaTypography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = colors.ink
                        )
                        Text(
                            text = if (isEditing) "Update stock, prices, or details" else "Create item with auto-assigned QR",
                            style = TindaTypography.bodySmall,
                            color = colors.muted
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(colors.cardCream)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = colors.ink
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Scrollable Form Fields
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (errorMessage != null) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (colors.isDark) Color(0xFF381E1C) else AlertRedLight,
                            border = BorderStroke(1.dp, AlertRedBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = errorMessage!!,
                                color = AlertRed,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    // Emoji Avatar Selector
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(text = "Product Icon / Emoji", style = TindaTypography.titleSmall, color = colors.ink)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(defaultEmojis) { emoji ->
                                val isSelected = emoji == selectedEmoji
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) colors.forest else colors.cardCream)
                                        .clickable { selectedEmoji = emoji },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = emoji, fontSize = 22.sp)
                                }
                            }
                        }
                    }

                    // Product Name
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it; errorMessage = null },
                        label = { Text("Product Name *") },
                        placeholder = { Text("e.g. Lucky Me Pancit Canton Kalamansi") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_product_name"),
                        shape = RoundedCornerShape(16.dp),
                        colors = textFieldColors,
                        singleLine = true
                    )

                    // Owner Quick Selection
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(text = "Store Owner / Capital Share *", style = TindaTypography.titleSmall, color = colors.ink)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(defaultOwners) { o ->
                                val isSelected = o.equals(owner, ignoreCase = true)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { owner = o },
                                    label = { Text(o) },
                                    border = BorderStroke(1.dp, if (isSelected) colors.forest else colors.cardBorder),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = colors.forest,
                                        selectedLabelColor = Color.White,
                                        containerColor = colors.cardCream,
                                        labelColor = colors.ink
                                    )
                                )
                            }
                        }
                    }

                    // Category Selection
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(text = "Category *", style = TindaTypography.titleSmall, color = colors.ink)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(defaultCategories) { cat ->
                                val isSelected = cat.equals(category, ignoreCase = true)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { category = cat },
                                    label = { Text(cat) },
                                    border = BorderStroke(1.dp, if (isSelected) colors.forest else colors.cardBorder),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = colors.forest,
                                        selectedLabelColor = Color.White,
                                        containerColor = colors.cardCream,
                                        labelColor = colors.ink
                                    )
                                )
                            }
                        }
                    }

                    // Supplier
                    OutlinedTextField(
                        value = supplier,
                        onValueChange = { supplier = it },
                        label = { Text("Supplier / Distributor") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = textFieldColors,
                        singleLine = true
                    )

                    // Prices: Cost Price & Selling Price
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = costPriceText,
                            onValueChange = { costPriceText = it; errorMessage = null },
                            label = { Text("Cost Price (₱)") },
                            prefix = { Text("₱ ") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_cost_price"),
                            shape = RoundedCornerShape(16.dp),
                            colors = textFieldColors,
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = sellingPriceText,
                            onValueChange = { sellingPriceText = it; errorMessage = null },
                            label = { Text("Selling Price (₱) *") },
                            prefix = { Text("₱ ") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_selling_price"),
                            shape = RoundedCornerShape(16.dp),
                            colors = textFieldColors,
                            singleLine = true
                        )
                    }

                    // Stock & Reorder Levels
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = quantityText,
                            onValueChange = { quantityText = it; errorMessage = null },
                            label = { Text("Quantity in Stock *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_quantity"),
                            shape = RoundedCornerShape(16.dp),
                            colors = textFieldColors,
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = reorderLevelText,
                            onValueChange = { reorderLevelText = it },
                            label = { Text("Reorder Alert Level") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            colors = textFieldColors,
                            singleLine = true
                        )
                    }

                    // SKU & QR Code
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = sku,
                            onValueChange = { sku = it },
                            label = { Text("SKU (Optional)") },
                            placeholder = { Text("Auto-generated") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            colors = textFieldColors,
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = qrCode,
                            onValueChange = { qrCode = it },
                            label = { Text("QR Value (Optional)") },
                            placeholder = { Text("e.g. TT-000001") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            colors = textFieldColors,
                            singleLine = true
                        )
                    }

                    // Expiry Date
                    OutlinedTextField(
                        value = expiryDate,
                        onValueChange = { expiryDate = it },
                        label = { Text("Expiry Date (Optional)") },
                        placeholder = { Text("YYYY-MM-DD") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = textFieldColors,
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                }

                // Action Buttons (Save / Cancel)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TindaSecondaryButton(
                        text = "Cancel",
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    )

                    TindaPrimaryButton(
                        text = if (isEditing) "Save Changes" else "Save Paninda",
                        onClick = {
                            if (name.isBlank()) {
                                errorMessage = "Product name is required!"
                                return@TindaPrimaryButton
                            }
                            if (owner.isBlank()) {
                                errorMessage = "Store owner is required!"
                                return@TindaPrimaryButton
                            }
                            val costCentavos = CurrencyHelper.parseToCentavos(costPriceText)
                            val sellingCentavos = CurrencyHelper.parseToCentavos(sellingPriceText)
                            if (sellingCentavos <= 0) {
                                errorMessage = "Selling price must be greater than zero!"
                                return@TindaPrimaryButton
                            }
                            val qty = quantityText.toIntOrNull()
                            if (qty == null || qty < 0) {
                                errorMessage = "Quantity must be 0 or higher!"
                                return@TindaPrimaryButton
                            }
                            val reorder = reorderLevelText.toIntOrNull() ?: 5
                            if (reorder < 0) {
                                errorMessage = "Reorder level cannot be negative!"
                                return@TindaPrimaryButton
                            }

                            onSave(
                                product?.id ?: 0L,
                                name,
                                owner,
                                category,
                                supplier,
                                sku,
                                qrCode,
                                costCentavos,
                                sellingCentavos,
                                qty,
                                reorder,
                                expiryDate,
                                selectedEmoji
                            )
                        },
                        modifier = Modifier
                            .weight(2f)
                            .testTag("submit_product_button")
                    )
                }
            }
        }
    }
}
