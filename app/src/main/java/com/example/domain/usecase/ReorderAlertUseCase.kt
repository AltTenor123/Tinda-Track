package com.example.domain.usecase

import com.example.domain.model.CurrencyHelper
import com.example.domain.model.Product
import com.example.domain.model.StockStatus
import com.example.domain.repository.ProductRepository

data class ReorderItem(
    val product: Product,
    val suggestedQuantity: Int,
    val estimatedCostCentavos: Long
)

data class SupplierReorderGroup(
    val supplier: String,
    val items: List<ReorderItem>,
    val totalEstimatedCostCentavos: Long
)

class ReorderAlertUseCase(
    private val productRepository: ProductRepository
) {
    suspend fun getReorderAlerts(): List<SupplierReorderGroup> {
        val allProducts = productRepository.getAllProducts()
        val lowOrOutProducts = allProducts.filter { it.status != StockStatus.IN_STOCK }

        val reorderItems = lowOrOutProducts.map { prod ->
            // Buffer suggestion: order up to 2x reorder level, at least 6 units
            val suggested = kotlin.math.max(6, (prod.reorderLevel * 2) - prod.quantity)
            ReorderItem(
                product = prod,
                suggestedQuantity = suggested,
                estimatedCostCentavos = prod.costPrice * suggested
            )
        }

        return reorderItems
            .groupBy { it.product.supplier }
            .map { (supplier, items) ->
                SupplierReorderGroup(
                    supplier = supplier,
                    items = items,
                    totalEstimatedCostCentavos = items.sumOf { it.estimatedCostCentavos }
                )
            }
            .sortedByDescending { it.items.size }
    }

    suspend fun formatReorderText(): String {
        val groups = getReorderAlerts()
        if (groups.isEmpty()) {
            return "All products are well-stocked! No reorders needed."
        }

        val sb = StringBuilder()
        sb.appendLine("📦 TINDA TRACK - SUGGESTED STORE REORDER")
        sb.appendLine("========================================")

        var grandTotalCentavos = 0L

        for (group in groups) {
            sb.appendLine("\n🏢 SUPPLIER: ${group.supplier.uppercase()}")
            sb.appendLine("----------------------------------------")
            for (item in group.items) {
                val statusTag = if (item.product.quantity == 0) "[OUT OF STOCK]" else "[LOW: ${item.product.quantity} left]"
                sb.appendLine("• ${item.product.name} $statusTag")
                sb.appendLine("  Order: ${item.suggestedQuantity} pcs @ ~${CurrencyHelper.formatCentavos(item.product.costPrice)} = ${CurrencyHelper.formatCentavos(item.estimatedCostCentavos)}")
            }
            sb.appendLine("Supplier Subtotal: ${CurrencyHelper.formatCentavos(group.totalEstimatedCostCentavos)}")
            grandTotalCentavos += group.totalEstimatedCostCentavos
        }

        sb.appendLine("\n========================================")
        sb.appendLine("TOTAL ESTIMATED RESTOCK BUDGET: ${CurrencyHelper.formatCentavos(grandTotalCentavos)}")
        return sb.toString()
    }
}
