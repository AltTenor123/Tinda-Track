package com.example.domain.usecase

import com.example.domain.model.Product
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class StockMovementUseCaseTest {

    private lateinit var fakeProductRepo: FakeProductRepository
    private lateinit var stockMovementUseCase: StockMovementUseCase

    @Before
    fun setup() {
        fakeProductRepo = FakeProductRepository()
        stockMovementUseCase = StockMovementUseCase(fakeProductRepo)
    }

    @Test
    fun stockIn_validQuantity_increasesStock() = runTest {
        val product = createProduct(id = 1L, initialStock = 10)
        fakeProductRepo.products[1L] = product

        val result = stockMovementUseCase.stockIn(
            productId = 1L,
            quantity = 5,
            reason = "Restocked from distributor"
        )

        assertTrue(result is StockMovementResult.Success)
        assertEquals(15, (result as StockMovementResult.Success).newQuantity)
        assertEquals(15, fakeProductRepo.products[1L]?.quantity)
    }

    @Test
    fun stockIn_zeroOrNegative_isRejected() = runTest {
        val product = createProduct(id = 1L, initialStock = 10)
        fakeProductRepo.products[1L] = product

        val zeroResult = stockMovementUseCase.stockIn(1L, 0, "Test")
        val negativeResult = stockMovementUseCase.stockIn(1L, -5, "Test")

        assertTrue(zeroResult is StockMovementResult.InvalidDelta)
        assertTrue(negativeResult is StockMovementResult.InvalidDelta)
    }

    @Test
    fun stockOut_exceedingStock_isRejected() = runTest {
        val product = createProduct(id = 1L, initialStock = 4)
        fakeProductRepo.products[1L] = product

        val result = stockMovementUseCase.stockOut(
            productId = 1L,
            quantity = 10, // Request 10, have 4
            reason = "Damaged packaging"
        )

        assertTrue(result is StockMovementResult.InsufficientStock)
        val failure = result as StockMovementResult.InsufficientStock
        assertEquals(4, failure.available)
        assertEquals(10, failure.requestedDeduction)
        assertEquals(4, fakeProductRepo.products[1L]?.quantity) // unchanged
    }

    @Test
    fun stockOut_validQuantity_decreasesStock() = runTest {
        val product = createProduct(id = 1L, initialStock = 10)
        fakeProductRepo.products[1L] = product

        val result = stockMovementUseCase.stockOut(
            productId = 1L,
            quantity = 4,
            reason = "Store consumption"
        )

        assertTrue(result is StockMovementResult.Success)
        assertEquals(6, (result as StockMovementResult.Success).newQuantity)
        assertEquals(6, fakeProductRepo.products[1L]?.quantity)
    }

    @Test
    fun adjustStock_setsExactCount_andRejectsNegative() = runTest {
        val product = createProduct(id = 1L, initialStock = 10)
        fakeProductRepo.products[1L] = product

        val negativeResult = stockMovementUseCase.adjustStock(1L, -2, "Inventory count")
        assertTrue(negativeResult is StockMovementResult.NegativeAdjustment)

        val validResult = stockMovementUseCase.adjustStock(1L, 22, "Physical count audit")
        assertTrue(validResult is StockMovementResult.Success)
        assertEquals(22, (validResult as StockMovementResult.Success).newQuantity)
        assertEquals(22, fakeProductRepo.products[1L]?.quantity)
    }

    @Test
    fun stockMovement_emptyReason_isRejected() = runTest {
        val product = createProduct(id = 1L, initialStock = 10)
        fakeProductRepo.products[1L] = product

        val inResult = stockMovementUseCase.stockIn(1L, 5, "")
        val outResult = stockMovementUseCase.stockOut(1L, 2, "   ")
        val adjResult = stockMovementUseCase.adjustStock(1L, 8, "")

        assertTrue(inResult is StockMovementResult.ReasonRequired)
        assertTrue(outResult is StockMovementResult.ReasonRequired)
        assertTrue(adjResult is StockMovementResult.ReasonRequired)
    }

    private fun createProduct(id: Long, initialStock: Int) = Product(
        id = id,
        name = "Safeguard Soap",
        owner = "Ate Susan",
        category = "Personal Care",
        supplier = "P&G",
        sku = "SG-60G",
        qrCode = "TT-000011",
        costPrice = 2100L,
        sellingPrice = 2600L,
        quantity = initialStock,
        reorderLevel = 5,
        createdAt = "2026-11-01 00:00:00",
        updatedAt = "2026-11-01 00:00:00"
    )
}
