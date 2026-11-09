package com.example.domain.repository

import com.example.domain.model.Product
import com.example.domain.model.StockMovement
import com.example.domain.model.StockMovementType
import com.example.domain.model.StockStatus
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    fun getAllProductsFlow(): Flow<List<Product>>
    suspend fun getAllProducts(): List<Product>
    suspend fun getProductById(id: Long): Product?
    suspend fun getProductByQr(code: String): Product?
    suspend fun searchProducts(
        query: String = "",
        category: String? = null,
        owner: String? = null,
        status: StockStatus? = null
    ): List<Product>
    suspend fun insertProduct(product: Product): Long
    suspend fun updateProduct(product: Product): Boolean
    suspend fun deleteProduct(id: Long): Boolean
    suspend fun applyStockMovement(
        productId: Long,
        type: StockMovementType,
        delta: Int,
        reason: String
    ): Boolean
    suspend fun getStockMovements(productId: Long? = null, limit: Int = 50): List<StockMovement>
    suspend fun generateNextQrCode(): String
    suspend fun getAllCategories(): List<String>
    suspend fun getAllOwners(): List<String>
}
