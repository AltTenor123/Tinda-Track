package com.example.domain.usecase

import com.example.domain.model.Utang
import com.example.domain.model.UtangStatus
import com.example.domain.repository.UtangRepository

sealed class UtangPaymentResult {
    data class Success(val updatedUtang: Utang) : UtangPaymentResult()
    object UtangNotFound : UtangPaymentResult()
    object InvalidAmount : UtangPaymentResult()
    object AlreadyFullyPaid : UtangPaymentResult()
    data class OverpaymentRejected(val maxAllowedCentavos: Long, val attemptedCentavos: Long) : UtangPaymentResult()
    data class Error(val message: String) : UtangPaymentResult()
}

class UtangPaymentUseCase(
    private val utangRepository: UtangRepository
) {
    suspend operator fun invoke(utangId: Long, paymentAmountCentavos: Long): UtangPaymentResult {
        if (paymentAmountCentavos <= 0L) {
            return UtangPaymentResult.InvalidAmount
        }

        val utang = utangRepository.getUtangById(utangId) ?: return UtangPaymentResult.UtangNotFound

        if (utang.status == UtangStatus.Paid || utang.paid >= utang.amount) {
            return UtangPaymentResult.AlreadyFullyPaid
        }

        val remainingBalance = utang.amount - utang.paid
        if (paymentAmountCentavos > remainingBalance) {
            return UtangPaymentResult.OverpaymentRejected(
                maxAllowedCentavos = remainingBalance,
                attemptedCentavos = paymentAmountCentavos
            )
        }

        val result = utangRepository.recordPayment(utangId, paymentAmountCentavos)
        return result.fold(
            onSuccess = { UtangPaymentResult.Success(it) },
            onFailure = { UtangPaymentResult.Error(it.message ?: "Failed to record payment") }
        )
    }
}
