package com.example.domain.usecase

import com.example.domain.model.*
import com.example.domain.repository.ProductRepository
import com.example.domain.repository.SalesRepository
import com.example.domain.repository.SalesSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class CheckoutUseCaseTest {

    private lateinit var fakeProductRepo: FakeProductRepository
    private lateinit var fakeSalesRepo: FakeSalesRepository
    private lateinit var checkoutUseCase: CheckoutUseCase

    @Before
    fun setup() {
        fakeProductRepo = FakeProductRepository()
        fakeSalesRepo = FakeSalesRepository(fakeProductRepo)
        checkoutUseCase = CheckoutUseCase(fakeSalesRepo, fakeProductRepo)
    }

    @Test
    fun checkout_emptyCart_returnsEmptyCartResult() = runTest {
        val result = checkoutUseCase.invoke(
            items = emptyList(),
            cashReceivedCentavos = 10000L
        )
        assertTrue(result is CheckoutResult.EmptyCart)
    }

    @Test
    fun checkout_insufficientStock_returnsInsufficientStockResult() = runTest {
        val product = createTestProduct(id = 1L, name = "Coke Mismo", stock = 3, price = 2000L)
        fakeProductRepo.products[1L] = product

        val cart = listOf(CartItem(product, quantity = 5)) // Request 5, have 3
        val result = checkoutUseCase.invoke(
            items = cart,
            cashReceivedCentavos = 10000L
        )

        assertTrue(result is CheckoutResult.InsufficientStock)
        val failure = result as CheckoutResult.InsufficientStock
        assertEquals("Coke Mismo", failure.productName)
        assertEquals(3, failure.available)
        assertEquals(5, failure.requested)
    }

    @Test
    fun checkout_insufficientCash_returnsInsufficientPaymentResult() = runTest {
        val product = createTestProduct(id = 1L, name = "Pancit Canton", stock = 10, price = 1600L)
        fakeProductRepo.products[1L] = product

        val cart = listOf(CartItem(product, quantity = 2)) // Total ₱32.00 (3200 centavos)
        val result = checkoutUseCase.invoke(
            items = cart,
            cashReceivedCentavos = 3000L // Paid ₱30.00
        )

        assertTrue(result is CheckoutResult.InsufficientPayment)
        val failure = result as CheckoutResult.InsufficientPayment
        assertEquals(3200L, failure.requiredCentavos)
        assertEquals(3000L, failure.receivedCentavos)
    }

    @Test
    fun checkout_validTransaction_succeedsWithCorrectChangeAndProfit() = runTest {
        val p1 = createTestProduct(id = 1L, name = "Coke Mismo", stock = 10, cost = 1600L, price = 2000L)
        val p2 = createTestProduct(id = 2L, name = "Piattos", stock = 5, cost = 2900L, price = 3500L)
        fakeProductRepo.products[1L] = p1
        fakeProductRepo.products[2L] = p2

        val cart = listOf(
            CartItem(p1, quantity = 2), // 2 * 20.00 = 40.00, cost = 32.00
            CartItem(p2, quantity = 1)  // 1 * 35.00 = 35.00, cost = 29.00
        )
        // Total = 75.00 (7500 centavos), Cost = 61.00 (6100 centavos), Profit = 14.00 (1400 centavos)
        val result = checkoutUseCase.invoke(
            items = cart,
            cashReceivedCentavos = 10000L // Paid ₱100.00
        )

        assertTrue(result is CheckoutResult.Success)
        val success = result as CheckoutResult.Success
        assertEquals(7500L, success.sale.total)
        assertEquals(10000L, success.sale.cashReceived)
        assertEquals(2500L, success.sale.change)
        assertEquals(1400L, success.sale.estimatedProfit)

        // Verify stock decremented
        assertEquals(8, fakeProductRepo.products[1L]?.quantity)
        assertEquals(4, fakeProductRepo.products[2L]?.quantity)
    }

    private fun createTestProduct(
        id: Long,
        name: String,
        stock: Int,
        cost: Long = 1000L,
        price: Long = 1500L
    ) = Product(
        id = id,
        name = name,
        owner = "Ate Susan",
        category = "Snacks",
        supplier = "Wholesale",
        sku = "SKU-$id",
        qrCode = "TT-%06d".format(id),
        costPrice = cost,
        sellingPrice = price,
        quantity = stock,
        reorderLevel = 5,
        createdAt = "2026-11-01 00:00:00",
        updatedAt = "2026-11-01 00:00:00"
    )
}

class FakeProductRepository : ProductRepository {
    val products = mutableMapOf<Long, Product>()
    val movements = mutableListOf<StockMovement>()

    override fun getAllProductsFlow(): Flow<List<Product>> = flowOf(products.values.toList())
    override suspend fun getAllProducts(): List<Product> = products.values.toList()
    override suspend fun getProductById(id: Long): Product? = products[id]
    override suspend fun getProductByQr(code: String): Product? = products.values.find { it.qrCode == code || it.sku == code }
    override suspend fun searchProducts(query: String, category: String?, owner: String?, status: StockStatus?): List<Product> = products.values.toList()
    override suspend fun insertProduct(product: Product): Long {
        val id = (products.keys.maxOrNull() ?: 0L) + 1
        products[id] = product.copy(id = id)
        return id
    }
    override suspend fun updateProduct(product: Product): Boolean {
        products[product.id] = product
        return true
    }
    override suspend fun deleteProduct(id: Long): Boolean = products.remove(id) != null
    override suspend fun applyStockMovement(productId: Long, type: StockMovementType, delta: Int, reason: String): Boolean {
        val p = products[productId] ?: return false
        val newQty = when (type) {
            StockMovementType.IN -> p.quantity + delta
            StockMovementType.OUT, StockMovementType.SALE -> p.quantity - delta
            StockMovementType.ADJUST -> delta
        }
        if (newQty < 0) return false
        products[productId] = p.copy(quantity = newQty)
        movements.add(StockMovement(productId = productId, type = type, delta = delta, reason = reason, createdAt = "2026-11-08 00:00:00"))
        return true
    }
    override suspend fun getStockMovements(productId: Long?, limit: Int): List<StockMovement> = movements
    override suspend fun generateNextQrCode(): String = "TT-%06d".format(products.size + 1)
    override suspend fun getAllCategories(): List<String> = emptyList()
    override suspend fun getAllOwners(): List<String> = emptyList()
}

class FakeSalesRepository(private val productRepo: FakeProductRepository) : SalesRepository {
    val sales = mutableListOf<Sale>()

    override fun getAllSalesFlow(): Flow<List<Sale>> = flowOf(sales)
    override suspend fun getAllSales(): List<Sale> = sales
    override suspend fun getSaleById(id: Long): Sale? = sales.find { it.id == id }

    override suspend fun recordSale(items: List<CartItem>, cashReceived: Long, cashier: String): Result<Sale> {
        val total = items.sumOf { it.subtotal }
        val totalCost = items.sumOf { it.totalCost }
        val profit = total - totalCost
        val change = cashReceived - total

        for (item in items) {
            productRepo.applyStockMovement(item.product.id, StockMovementType.SALE, item.quantity, "Sold")
        }

        val sale = Sale(
            id = (sales.size + 1).toLong(),
            receiptNo = "RCP-TEST-${sales.size + 1}",
            createdAt = "2026-11-08 12:00:00",
            cashier = cashier,
            total = total,
            cashReceived = cashReceived,
            change = change,
            estimatedProfit = profit,
            items = items.map {
                SaleItem(
                    productId = it.product.id,
                    productName = it.product.name,
                    quantity = it.quantity,
                    unitPrice = it.product.sellingPrice,
                    unitCost = it.product.costPrice
                )
            }
        )
        sales.add(sale)
        return Result.success(sale)
    }

    override suspend fun getTodaySalesSummary(): SalesSummary = SalesSummary(0, 0, 0)
    override suspend fun getTotalSalesSummary(): SalesSummary = SalesSummary(0, 0, 0)
    override suspend fun getDailySalesGrouped(): Map<String, List<Sale>> = emptyMap()
}
