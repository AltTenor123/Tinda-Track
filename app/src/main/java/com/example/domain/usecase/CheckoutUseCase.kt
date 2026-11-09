package com.example.domain.usecase

import com.example.domain.model.CartItem
import com.example.domain.model.Sale
import com.example.domain.repository.ProductRepository
import com.example.domain.repository.SalesRepository

sealed class CheckoutResult {
    data class Success(val sale: Sale) : CheckoutResult()
    object EmptyCart : CheckoutResult()
    data class InsufficientStock(val productName: String, val available: Int, val requested: Int) : CheckoutResult()
    data class InsufficientPayment(val requiredCentavos: Long, val receivedCentavos: Long) : CheckoutResult()
    data class Error(val message: String) : CheckoutResult()
}

class CheckoutUseCase(
    private val salesRepository: SalesRepository,
    private val productRepository: ProductRepository
) {
    suspend operator fun invoke(
        items: List<CartItem>,
        cashReceivedCentavos: Long,
        cashier: String = "Tindero/Tindera"
    ): CheckoutResult {
        if (items.isEmpty()) {
            return CheckoutResult.EmptyCart
        }

        // Validate quantities and live stock
        for (item in items) {
            if (item.quantity <= 0) {
                return CheckoutResult.Error("Invalid quantity for '${item.product.name}'")
            }
            val liveProduct = productRepository.getProductById(item.product.id)
                ?: return CheckoutResult.Error("Product '${item.product.name}' not found")

            if (item.quantity > liveProduct.quantity) {
                return CheckoutResult.InsufficientStock(
                    productName = liveProduct.name,
                    available = liveProduct.quantity,
                    requested = item.quantity
                )
            }
        }

        val totalCentavos = items.sumOf { it.subtotal }
        if (cashReceivedCentavos < totalCentavos) {
            return CheckoutResult.InsufficientPayment(
                requiredCentavos = totalCentavos,
                receivedCentavos = cashReceivedCentavos
            )
        }

        val recordResult = salesRepository.recordSale(
            items = items,
            cashReceived = cashReceivedCentavos,
            cashier = cashier
        )

        return recordResult.fold(
            onSuccess = { CheckoutResult.Success(it) },
            onFailure = { CheckoutResult.Error(it.message ?: "Failed to complete transaction") }
        )
    }
}
