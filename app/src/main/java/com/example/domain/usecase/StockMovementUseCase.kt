package com.example.domain.usecase

import com.example.domain.model.StockMovementType
import com.example.domain.repository.ProductRepository

sealed class StockMovementResult {
    data class Success(val newQuantity: Int) : StockMovementResult()
    object ProductNotFound : StockMovementResult()
    object InvalidDelta : StockMovementResult()
    data class InsufficientStock(val available: Int, val requestedDeduction: Int) : StockMovementResult()
    data class NegativeAdjustment(val attemptedValue: Int) : StockMovementResult()
    object ReasonRequired : StockMovementResult()
    data class Error(val message: String) : StockMovementResult()
}

class StockMovementUseCase(
    private val productRepository: ProductRepository
) {
    suspend fun stockIn(productId: Long, quantity: Int, reason: String): StockMovementResult {
        if (quantity <= 0) return StockMovementResult.InvalidDelta
        if (reason.isBlank()) return StockMovementResult.ReasonRequired

        val product = productRepository.getProductById(productId) ?: return StockMovementResult.ProductNotFound
        val success = productRepository.applyStockMovement(productId, StockMovementType.IN, quantity, reason.trim())
        return if (success) {
            StockMovementResult.Success(product.quantity + quantity)
        } else {
            StockMovementResult.Error("Database update failed")
        }
    }

    suspend fun stockOut(productId: Long, quantity: Int, reason: String): StockMovementResult {
        if (quantity <= 0) return StockMovementResult.InvalidDelta
        if (reason.isBlank()) return StockMovementResult.ReasonRequired

        val product = productRepository.getProductById(productId) ?: return StockMovementResult.ProductNotFound
        if (quantity > product.quantity) {
            return StockMovementResult.InsufficientStock(
                available = product.quantity,
                requestedDeduction = quantity
            )
        }

        val success = productRepository.applyStockMovement(productId, StockMovementType.OUT, quantity, reason.trim())
        return if (success) {
            StockMovementResult.Success(product.quantity - quantity)
        } else {
            StockMovementResult.Error("Database update failed")
        }
    }

    suspend fun adjustStock(productId: Long, exactQuantity: Int, reason: String): StockMovementResult {
        if (exactQuantity < 0) return StockMovementResult.NegativeAdjustment(exactQuantity)
        if (reason.isBlank()) return StockMovementResult.ReasonRequired

        val product = productRepository.getProductById(productId) ?: return StockMovementResult.ProductNotFound
        val success = productRepository.applyStockMovement(productId, StockMovementType.ADJUST, exactQuantity, reason.trim())
        return if (success) {
            StockMovementResult.Success(exactQuantity)
        } else {
            StockMovementResult.Error("Database update failed")
        }
    }
}
