package com.example.domain.model

import kotlinx.serialization.Serializable

/**
 * Money is stored as Long centavos (e.g., ₱25.50 -> 2550L).
 * This completely avoids floating-point rounding errors in retail calculations.
 */
object CurrencyHelper {
    fun formatCentavos(centavos: Long): String {
        val pesos = centavos / 100
        val remainder = kotlin.math.abs(centavos % 100)
        return "₱%,d.%02d".format(pesos, remainder)
    }

    fun parseToCentavos(amountText: String): Long {
        val clean = amountText.replace("₱", "").replace(",", "").trim()
        if (clean.isBlank()) return 0L
        val parts = clean.split(".")
        val pesos = parts[0].toLongOrNull() ?: 0L
        val centavos = if (parts.size > 1) {
            val centPart = parts[1].take(2).padEnd(2, '0')
            centPart.toLongOrNull() ?: 0L
        } else 0L
        return (pesos * 100) + centavos
    }
}

enum class StockStatus {
    OUT_OF_STOCK,
    LOW_STOCK,
    IN_STOCK
}

@Serializable
data class Product(
    val id: Long = 0,
    val name: String,
    val owner: String,
    val category: String,
    val supplier: String,
    val sku: String,
    val qrCode: String,
    val costPrice: Long,      // centavos
    val sellingPrice: Long,   // centavos
    val quantity: Int,
    val reorderLevel: Int,
    val expiryDate: String? = null, // YYYY-MM-DD
    val photoPath: String? = null,
    val createdAt: String,
    val updatedAt: String
) {
    val status: StockStatus
        get() = when {
            quantity <= 0 -> StockStatus.OUT_OF_STOCK
            quantity <= reorderLevel -> StockStatus.LOW_STOCK
            else -> StockStatus.IN_STOCK
        }

    val profitMarginCentavos: Long
        get() = sellingPrice - costPrice

    val totalStockValueCost: Long
        get() = costPrice * quantity

    val totalStockValueRetail: Long
        get() = sellingPrice * quantity
}

enum class StockMovementType {
    IN,
    OUT,
    ADJUST,
    SALE
}

@Serializable
data class StockMovement(
    val id: Long = 0,
    val productId: Long,
    val type: StockMovementType,
    val delta: Int,
    val reason: String,
    val createdAt: String
)

@Serializable
data class Sale(
    val id: Long = 0,
    val receiptNo: String,
    val createdAt: String,
    val cashier: String,
    val total: Long,            // centavos
    val cashReceived: Long,     // centavos
    val change: Long,           // centavos
    val estimatedProfit: Long,  // centavos
    val items: List<SaleItem> = emptyList()
)

@Serializable
data class SaleItem(
    val id: Long = 0,
    val saleId: Long = 0,
    val productId: Long,
    val productName: String,
    val quantity: Int,
    val unitPrice: Long,        // centavos
    val unitCost: Long          // centavos
) {
    val subtotal: Long get() = unitPrice * quantity
    val profit: Long get() = (unitPrice - unitCost) * quantity
}

enum class UtangStatus {
    Unpaid,
    Partial,
    Paid
}

@Serializable
data class Utang(
    val id: Long = 0,
    val customerName: String,
    val itemsNote: String,
    val amount: Long,           // centavos
    val paid: Long,             // centavos
    val status: UtangStatus,
    val createdAt: String,
    val updatedAt: String,
    val payments: List<UtangPayment> = emptyList()
) {
    val balance: Long get() = kotlin.math.max(0L, amount - paid)
}

@Serializable
data class UtangPayment(
    val id: Long = 0,
    val utangId: Long,
    val amount: Long,           // centavos
    val createdAt: String
)

@Serializable
data class CartItem(
    val product: Product,
    var quantity: Int
) {
    val subtotal: Long get() = product.sellingPrice * quantity
    val totalCost: Long get() = product.costPrice * quantity
    val estimatedProfit: Long get() = subtotal - totalCost
}
