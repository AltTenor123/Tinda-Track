package com.example.data.repository

import android.content.ContentValues
import com.example.data.db.TindaDatabaseHelper
import com.example.domain.model.CartItem
import com.example.domain.model.Sale
import com.example.domain.model.SaleItem
import com.example.domain.model.StockMovementType
import com.example.domain.repository.SalesRepository
import com.example.domain.repository.SalesSummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SalesRepositoryImpl(
    private val dbHelper: TindaDatabaseHelper
) : SalesRepository {

    private val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
    private val dateOnlyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val receiptDateFormat = SimpleDateFormat("yyMMdd", Locale.US)

    private fun currentTimestamp(): String = dateTimeFormat.format(Date())
    private fun currentDateOnly(): String = dateOnlyFormat.format(Date())

    override fun getAllSalesFlow(): Flow<List<Sale>> = flow {
        emit(getAllSales())
        dbHelper.databaseUpdates.collect {
            emit(getAllSales())
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun getAllSales(): List<Sale> = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        val salesList = mutableListOf<Sale>()
        val cursor = db.rawQuery("SELECT * FROM sales ORDER BY id DESC", null)
        cursor.use {
            while (it.moveToNext()) {
                val sale = dbHelper.cursorToSale(it)
                val items = getItemsForSale(sale.id)
                salesList.add(sale.copy(items = items))
            }
        }
        salesList
    }

    override suspend fun getSaleById(id: Long): Sale? = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM sales WHERE id = ?", arrayOf(id.toString()))
        cursor.use {
            if (it.moveToNext()) {
                val sale = dbHelper.cursorToSale(it)
                val items = getItemsForSale(sale.id)
                sale.copy(items = items)
            } else null
        }
    }

    private fun getItemsForSale(saleId: Long): List<SaleItem> {
        val db = dbHelper.readableDatabase
        val list = mutableListOf<SaleItem>()
        val cursor = db.rawQuery("SELECT * FROM sale_items WHERE sale_id = ?", arrayOf(saleId.toString()))
        cursor.use {
            while (it.moveToNext()) {
                list.add(dbHelper.cursorToSaleItem(it))
            }
        }
        return list
    }

    override suspend fun recordSale(
        items: List<CartItem>,
        cashReceived: Long,
        cashier: String
    ): Result<Sale> = withContext(Dispatchers.IO) {
        if (items.isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("Cart is empty"))
        }

        val total = items.sumOf { it.subtotal }
        if (cashReceived < total) {
            return@withContext Result.failure(
                IllegalArgumentException("Payment insufficient. Received ₱${cashReceived / 100.0}, needed ₱${total / 100.0}")
            )
        }

        val totalCost = items.sumOf { it.totalCost }
        val estimatedProfit = total - totalCost
        val change = cashReceived - total
        val now = currentTimestamp()

        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            // 1. Re-check live stock inside transaction
            for (item in items) {
                val checkCursor = db.rawQuery(
                    "SELECT quantity, name FROM products WHERE id = ?",
                    arrayOf(item.product.id.toString())
                )
                val (currentStock, productName) = checkCursor.use {
                    if (it.moveToNext()) {
                        Pair(it.getInt(0), it.getString(1))
                    } else {
                        return@withContext Result.failure(
                            IllegalStateException("Product ${item.product.name} no longer exists")
                        )
                    }
                }

                if (item.quantity > currentStock) {
                    return@withContext Result.failure(
                        IllegalStateException("Insufficient stock for '$productName'. Available: $currentStock, In cart: ${item.quantity}")
                    )
                }
            }

            // 2. Generate unique receipt number (e.g. RCP-261108-0042)
            val countCursor = db.rawQuery("SELECT COUNT(*) FROM sales", null)
            val currentCount = countCursor.use { if (it.moveToNext()) it.getInt(0) else 0 }
            val receiptNo = "RCP-${receiptDateFormat.format(Date())}-${"%04d".format(currentCount + 1)}"

            // 3. Insert Sale
            val saleCv = ContentValues().apply {
                put("receipt_no", receiptNo)
                put("created_at", now)
                put("cashier", cashier)
                put("total", total)
                put("cash_received", cashReceived)
                put("change", change)
                put("estimated_profit", estimatedProfit)
            }
            val saleId = db.insertOrThrow("sales", null, saleCv)

            val recordedItems = mutableListOf<SaleItem>()

            // 4. Insert Sale Items, update Product stock, and record stock_movements
            for (item in items) {
                val itemCv = ContentValues().apply {
                    put("sale_id", saleId)
                    put("product_id", item.product.id)
                    put("product_name", item.product.name)
                    put("quantity", item.quantity)
                    put("unit_price", item.product.sellingPrice)
                    put("unit_cost", item.product.costPrice)
                }
                val itemId = db.insertOrThrow("sale_items", null, itemCv)
                recordedItems.add(
                    SaleItem(
                        id = itemId,
                        saleId = saleId,
                        productId = item.product.id,
                        productName = item.product.name,
                        quantity = item.quantity,
                        unitPrice = item.product.sellingPrice,
                        unitCost = item.product.costPrice
                    )
                )

                // Decrement live stock
                db.execSQL(
                    "UPDATE products SET quantity = quantity - ?, updated_at = ? WHERE id = ?",
                    arrayOf<Any>(item.quantity, now, item.product.id)
                )

                // Record stock movement
                val movCv = ContentValues().apply {
                    put("product_id", item.product.id)
                    put("type", StockMovementType.SALE.name)
                    put("delta", -item.quantity)
                    put("reason", "Sold in $receiptNo")
                    put("created_at", now)
                }
                db.insert("stock_movements", null, movCv)
            }

            db.setTransactionSuccessful()
            dbHelper.notifyUpdated()

            val completedSale = Sale(
                id = saleId,
                receiptNo = receiptNo,
                createdAt = now,
                cashier = cashier,
                total = total,
                cashReceived = cashReceived,
                change = change,
                estimatedProfit = estimatedProfit,
                items = recordedItems
            )
            Result.success(completedSale)
        } finally {
            db.endTransaction()
        }
    }

    override suspend fun getTodaySalesSummary(): SalesSummary = withContext(Dispatchers.IO) {
        val today = currentDateOnly()
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT SUM(total), SUM(estimated_profit), COUNT(*) FROM sales WHERE created_at LIKE ?",
            arrayOf("$today%")
        )
        cursor.use {
            if (it.moveToNext()) {
                SalesSummary(
                    totalRevenueCentavos = if (it.isNull(0)) 0L else it.getLong(0),
                    estimatedProfitCentavos = if (it.isNull(1)) 0L else it.getLong(1),
                    transactionCount = if (it.isNull(2)) 0 else it.getInt(2)
                )
            } else SalesSummary(0L, 0L, 0)
        }
    }

    override suspend fun getTotalSalesSummary(): SalesSummary = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT SUM(total), SUM(estimated_profit), COUNT(*) FROM sales",
            null
        )
        cursor.use {
            if (it.moveToNext()) {
                SalesSummary(
                    totalRevenueCentavos = if (it.isNull(0)) 0L else it.getLong(0),
                    estimatedProfitCentavos = if (it.isNull(1)) 0L else it.getLong(1),
                    transactionCount = if (it.isNull(2)) 0 else it.getInt(2)
                )
            } else SalesSummary(0L, 0L, 0)
        }
    }

    override suspend fun getDailySalesGrouped(): Map<String, List<Sale>> = withContext(Dispatchers.IO) {
        val allSales = getAllSales()
        allSales.groupBy { sale ->
            if (sale.createdAt.length >= 10) sale.createdAt.substring(0, 10) else sale.createdAt
        }
    }
}
