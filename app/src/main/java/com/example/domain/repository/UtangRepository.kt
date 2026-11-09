package com.example.domain.repository

import com.example.domain.model.Utang
import com.example.domain.model.UtangPayment
import kotlinx.coroutines.flow.Flow

interface UtangRepository {
    fun getAllUtangFlow(): Flow<List<Utang>>
    suspend fun getAllUtang(): List<Utang>
    suspend fun getUtangById(id: Long): Utang?
    suspend fun createUtang(
        customerName: String,
        itemsNote: String,
        amount: Long
    ): Long
    suspend fun recordPayment(utangId: Long, paymentAmount: Long): Result<Utang>
    suspend fun deleteUtang(id: Long): Boolean
    suspend fun getOpenUtangTotal(): Long
    suspend fun getPaymentsForUtang(utangId: Long): List<UtangPayment>
}
