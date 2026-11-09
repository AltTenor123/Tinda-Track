package com.example.domain.repository

import com.example.domain.model.CartItem
import com.example.domain.model.Sale
import kotlinx.coroutines.flow.Flow

data class SalesSummary(
    val totalRevenueCentavos: Long,
    val estimatedProfitCentavos: Long,
    val transactionCount: Int
)

interface SalesRepository {
    fun getAllSalesFlow(): Flow<List<Sale>>
    suspend fun getAllSales(): List<Sale>
    suspend fun getSaleById(id: Long): Sale?
    suspend fun recordSale(
        items: List<CartItem>,
        cashReceived: Long,
        cashier: String = "Tindero/Tindera"
    ): Result<Sale>
    suspend fun getTodaySalesSummary(): SalesSummary
    suspend fun getTotalSalesSummary(): SalesSummary
    suspend fun getDailySalesGrouped(): Map<String, List<Sale>>
}
