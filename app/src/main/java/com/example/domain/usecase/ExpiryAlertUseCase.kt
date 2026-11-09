package com.example.domain.usecase

import com.example.domain.model.Product
import com.example.domain.repository.ProductRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

enum class ExpiryUrgency {
    EXPIRED,
    EXPIRING_SOON, // <= 30 days
    SAFE
}

data class ExpiryAlertItem(
    val product: Product,
    val expiryDate: String,
    val daysRemaining: Long,
    val urgency: ExpiryUrgency
)

class ExpiryAlertUseCase(
    private val productRepository: ProductRepository
) {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    suspend fun getExpiryAlerts(referenceDate: Date = Date()): List<ExpiryAlertItem> {
        val allProducts = productRepository.getAllProducts()
        val list = mutableListOf<ExpiryAlertItem>()

        for (product in allProducts) {
            val dateStr = product.expiryDate ?: continue
            val expDate = try {
                dateFormat.parse(dateStr)
            } catch (_: Exception) {
                null
            } ?: continue

            val diffMillis = expDate.time - referenceDate.time
            val daysRemaining = TimeUnit.MILLISECONDS.toDays(diffMillis)

            val urgency = when {
                daysRemaining <= 0 -> ExpiryUrgency.EXPIRED
                daysRemaining <= 30 -> ExpiryUrgency.EXPIRING_SOON
                else -> ExpiryUrgency.SAFE
            }

            if (urgency != ExpiryUrgency.SAFE) {
                list.add(
                    ExpiryAlertItem(
                        product = product,
                        expiryDate = dateStr,
                        daysRemaining = daysRemaining,
                        urgency = urgency
                    )
                )
            }
        }

        return list.sortedWith(
            compareBy<ExpiryAlertItem> { it.urgency == ExpiryUrgency.EXPIRED }
                .reversed()
                .thenBy { it.daysRemaining }
        )
    }
}
