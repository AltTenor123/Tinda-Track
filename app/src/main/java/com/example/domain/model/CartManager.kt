package com.example.domain.model

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class CartManager {
    private val _items = MutableStateFlow<List<CartItem>>(emptyList())
    val items: StateFlow<List<CartItem>> = _items.asStateFlow()

    fun addToCart(product: Product, quantity: Int = 1): Boolean {
        if (product.quantity <= 0) return false // Cannot add out of stock item

        var success = true
        _items.update { current ->
            val existing = current.find { it.product.id == product.id }
            if (existing != null) {
                val newQty = existing.quantity + quantity
                if (newQty > product.quantity) {
                    success = false
                    current
                } else {
                    current.map {
                        if (it.product.id == product.id) it.copy(quantity = newQty) else it
                    }
                }
            } else {
                val addQty = quantity.coerceAtMost(product.quantity)
                current + CartItem(product = product, quantity = addQty)
            }
        }
        return success
    }

    fun updateQuantity(productId: Long, newQuantity: Int) {
        _items.update { current ->
            if (newQuantity <= 0) {
                current.filter { it.product.id != productId }
            } else {
                current.map {
                    if (it.product.id == productId) {
                        val maxAllowed = it.product.quantity
                        it.copy(quantity = newQuantity.coerceAtMost(maxAllowed))
                    } else it
                }
            }
        }
    }

    fun removeItem(productId: Long) {
        _items.update { current -> current.filter { it.product.id != productId } }
    }

    fun clearCart() {
        _items.value = emptyList()
    }

    val totalCentavos: Long
        get() = _items.value.sumOf { it.subtotal }

    val totalCostCentavos: Long
        get() = _items.value.sumOf { it.totalCost }

    val estimatedProfitCentavos: Long
        get() = totalCentavos - totalCostCentavos

    val itemCount: Int
        get() = _items.value.sumOf { it.quantity }
}
