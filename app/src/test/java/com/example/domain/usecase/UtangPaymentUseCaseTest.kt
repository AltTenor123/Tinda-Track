package com.example.domain.usecase

import com.example.domain.model.Utang
import com.example.domain.model.UtangPayment
import com.example.domain.model.UtangStatus
import com.example.domain.repository.UtangRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class UtangPaymentUseCaseTest {

    private lateinit var fakeUtangRepo: FakeUtangRepository
    private lateinit var utangPaymentUseCase: UtangPaymentUseCase

    @Before
    fun setup() {
        fakeUtangRepo = FakeUtangRepository()
        utangPaymentUseCase = UtangPaymentUseCase(fakeUtangRepo)
    }

    @Test
    fun payment_zeroOrNegative_isRejected() = runTest {
        val utang = createUtang(id = 1L, total = 5000L, paid = 0L)
        fakeUtangRepo.records[1L] = utang

        val zeroResult = utangPaymentUseCase.invoke(1L, 0L)
        val negResult = utangPaymentUseCase.invoke(1L, -100L)

        assertTrue(zeroResult is UtangPaymentResult.InvalidAmount)
        assertTrue(negResult is UtangPaymentResult.InvalidAmount)
    }

    @Test
    fun payment_overpayment_isRejected() = runTest {
        // Total ₱50.00 (5000L), already paid ₱20.00 (2000L), remaining balance ₱30.00 (3000L)
        val utang = createUtang(id = 1L, total = 5000L, paid = 2000L)
        fakeUtangRepo.records[1L] = utang

        // Attempting to pay ₱40.00 (4000L) which exceeds 3000L
        val result = utangPaymentUseCase.invoke(1L, 4000L)

        assertTrue(result is UtangPaymentResult.OverpaymentRejected)
        val rejected = result as UtangPaymentResult.OverpaymentRejected
        assertEquals(3000L, rejected.maxAllowedCentavos)
        assertEquals(4000L, rejected.attemptedCentavos)
        assertEquals(2000L, fakeUtangRepo.records[1L]?.paid) // untouched
    }

    @Test
    fun payment_partialPayment_updatesStatusToPartial() = runTest {
        // Total ₱50.00, paid 0
        val utang = createUtang(id = 1L, total = 5000L, paid = 0L)
        fakeUtangRepo.records[1L] = utang

        val result = utangPaymentUseCase.invoke(1L, 2000L) // Pay ₱20.00

        assertTrue(result is UtangPaymentResult.Success)
        val updated = (result as UtangPaymentResult.Success).updatedUtang
        assertEquals(2000L, updated.paid)
        assertEquals(UtangStatus.Partial, updated.status)
        assertEquals(3000L, updated.balance)
    }

    @Test
    fun payment_fullPayment_updatesStatusToPaid() = runTest {
        // Total ₱50.00, already paid ₱20.00
        val utang = createUtang(id = 1L, total = 5000L, paid = 2000L)
        fakeUtangRepo.records[1L] = utang

        val result = utangPaymentUseCase.invoke(1L, 3000L) // Pay exact remaining ₱30.00

        assertTrue(result is UtangPaymentResult.Success)
        val updated = (result as UtangPaymentResult.Success).updatedUtang
        assertEquals(5000L, updated.paid)
        assertEquals(UtangStatus.Paid, updated.status)
        assertEquals(0L, updated.balance)
    }

    @Test
    fun payment_alreadyPaidUtang_isRejected() = runTest {
        val utang = createUtang(id = 1L, total = 5000L, paid = 5000L, status = UtangStatus.Paid)
        fakeUtangRepo.records[1L] = utang

        val result = utangPaymentUseCase.invoke(1L, 1000L)
        assertTrue(result is UtangPaymentResult.AlreadyFullyPaid)
    }

    private fun createUtang(id: Long, total: Long, paid: Long, status: UtangStatus = UtangStatus.Unpaid) = Utang(
        id = id,
        customerName = "Aling Marites",
        itemsNote = "2x Pancit Canton, 1x Coke",
        amount = total,
        paid = paid,
        status = if (paid >= total) UtangStatus.Paid else if (paid > 0) UtangStatus.Partial else status,
        createdAt = "2026-11-04 12:00:00",
        updatedAt = "2026-11-04 12:00:00"
    )
}

class FakeUtangRepository : UtangRepository {
    val records = mutableMapOf<Long, Utang>()
    val payments = mutableListOf<UtangPayment>()

    override fun getAllUtangFlow(): Flow<List<Utang>> = flowOf(records.values.toList())
    override suspend fun getAllUtang(): List<Utang> = records.values.toList()
    override suspend fun getUtangById(id: Long): Utang? = records[id]
    override suspend fun createUtang(customerName: String, itemsNote: String, amount: Long): Long {
        val id = (records.keys.maxOrNull() ?: 0L) + 1
        records[id] = Utang(id = id, customerName = customerName, itemsNote = itemsNote, amount = amount, paid = 0L, status = UtangStatus.Unpaid, createdAt = "2026-11-08 00:00:00", updatedAt = "2026-11-08 00:00:00")
        return id
    }
    override suspend fun recordPayment(utangId: Long, paymentAmount: Long): Result<Utang> {
        val u = records[utangId] ?: return Result.failure(IllegalArgumentException("Not found"))
        val newPaid = u.paid + paymentAmount
        val newStatus = when {
            newPaid >= u.amount -> UtangStatus.Paid
            newPaid > 0 -> UtangStatus.Partial
            else -> UtangStatus.Unpaid
        }
        val updated = u.copy(paid = newPaid, status = newStatus)
        records[utangId] = updated
        payments.add(UtangPayment(utangId = utangId, amount = paymentAmount, createdAt = "2026-11-08 00:00:00"))
        return Result.success(updated)
    }
    override suspend fun deleteUtang(id: Long): Boolean = records.remove(id) != null
    override suspend fun getOpenUtangTotal(): Long = records.values.filter { it.status != UtangStatus.Paid }.sumOf { it.balance }
    override suspend fun getPaymentsForUtang(utangId: Long): List<UtangPayment> = payments.filter { it.utangId == utangId }
}
