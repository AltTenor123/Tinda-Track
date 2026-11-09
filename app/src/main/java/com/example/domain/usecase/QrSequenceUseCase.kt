package com.example.domain.usecase

import com.example.domain.repository.ProductRepository

class QrSequenceUseCase(
    private val productRepository: ProductRepository
) {
    suspend fun generateNextCode(): String {
        return productRepository.generateNextQrCode()
    }

    fun sanitizeScannedCode(rawCode: String): String {
        return rawCode.trim().uppercase()
    }

    fun isValidQrCode(code: String): Boolean {
        val trimmed = code.trim()
        return trimmed.isNotBlank() && trimmed.length >= 3
    }
}
