package com.example.data.repository

import android.content.ContentValues
import com.example.data.db.TindaDatabaseHelper
import com.example.domain.model.Product
import com.example.domain.model.StockMovement
import com.example.domain.model.StockMovementType
import com.example.domain.model.StockStatus
import com.example.domain.repository.ProductRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProductRepositoryImpl(
    private val dbHelper: TindaDatabaseHelper
) : ProductRepository {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

    private fun currentTimestamp(): String = dateFormat.format(Date())

    override fun getAllProductsFlow(): Flow<List<Product>> = flow {
        emit(getAllProducts())
        dbHelper.databaseUpdates.collect {
            emit(getAllProducts())
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun getAllProducts(): List<Product> = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        val list = mutableListOf<Product>()
        val cursor = db.rawQuery("SELECT * FROM products ORDER BY name ASC", null)
        cursor.use {
            while (it.moveToNext()) {
                list.add(dbHelper.cursorToProduct(it))
            }
        }
        list
    }

    override suspend fun getProductById(id: Long): Product? = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM products WHERE id = ?", arrayOf(id.toString()))
        cursor.use {
            if (it.moveToNext()) {
                dbHelper.cursorToProduct(it)
            } else null
        }
    }

    override suspend fun getProductByQr(code: String): Product? = withContext(Dispatchers.IO) {
        val cleanCode = code.trim()
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM products WHERE qr_code = ? OR sku = ? LIMIT 1",
            arrayOf(cleanCode, cleanCode)
        )
        cursor.use {
            if (it.moveToNext()) {
                dbHelper.cursorToProduct(it)
            } else null
        }
    }

    override suspend fun searchProducts(
        query: String,
        category: String?,
        owner: String?,
        status: StockStatus?
    ): List<Product> = withContext(Dispatchers.IO) {
        val all = getAllProducts()
        val q = query.trim().lowercase()

        all.filter { prod ->
            val matchesQuery = q.isEmpty() ||
                    prod.name.lowercase().contains(q) ||
                    prod.sku.lowercase().contains(q) ||
                    prod.qrCode.lowercase().contains(q) ||
                    prod.category.lowercase().contains(q) ||
                    prod.owner.lowercase().contains(q) ||
                    prod.supplier.lowercase().contains(q)

            val matchesCategory = category.isNullOrBlank() || prod.category.equals(category, ignoreCase = true)
            val matchesOwner = owner.isNullOrBlank() || prod.owner.equals(owner, ignoreCase = true)
            val matchesStatus = status == null || prod.status == status

            matchesQuery && matchesCategory && matchesOwner && matchesStatus
        }
    }

    override suspend fun insertProduct(product: Product): Long = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        val now = currentTimestamp()
        val cv = ContentValues().apply {
            put("name", product.name.trim())
            put("owner", product.owner.trim())
            put("category", product.category.trim())
            put("supplier", product.supplier.trim())
            put("sku", product.sku.trim())
            put("qr_code", product.qrCode.trim())
            put("cost_price", product.costPrice)
            put("selling_price", product.sellingPrice)
            put("quantity", product.quantity)
            put("reorder_level", product.reorderLevel)
            put("expiry_date", product.expiryDate)
            put("photo_path", product.photoPath)
            put("created_at", if (product.createdAt.isBlank()) now else product.createdAt)
            put("updated_at", now)
        }
        val id = db.insertOrThrow("products", null, cv)
        if (id != -1L) {
            // Record initial stock creation movement if quantity > 0
            if (product.quantity > 0) {
                val movCv = ContentValues().apply {
                    put("product_id", id)
                    put("type", StockMovementType.IN.name)
                    put("delta", product.quantity)
                    put("reason", "Initial inventory count")
                    put("created_at", now)
                }
                db.insert("stock_movements", null, movCv)
            }
            dbHelper.notifyUpdated()
        }
        id
    }

    override suspend fun updateProduct(product: Product): Boolean = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        val now = currentTimestamp()
        val cv = ContentValues().apply {
            put("name", product.name.trim())
            put("owner", product.owner.trim())
            put("category", product.category.trim())
            put("supplier", product.supplier.trim())
            put("sku", product.sku.trim())
            put("qr_code", product.qrCode.trim())
            put("cost_price", product.costPrice)
            put("selling_price", product.sellingPrice)
            put("reorder_level", product.reorderLevel)
            put("expiry_date", product.expiryDate)
            put("photo_path", product.photoPath)
            put("updated_at", now)
        }
        val affected = db.update("products", cv, "id = ?", arrayOf(product.id.toString()))
        if (affected > 0) {
            dbHelper.notifyUpdated()
            true
        } else false
    }

    override suspend fun deleteProduct(id: Long): Boolean = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        val affected = db.delete("products", "id = ?", arrayOf(id.toString()))
        if (affected > 0) {
            dbHelper.notifyUpdated()
            true
        } else false
    }

    override suspend fun applyStockMovement(
        productId: Long,
        type: StockMovementType,
        delta: Int,
        reason: String
    ): Boolean = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            val cursor = db.rawQuery("SELECT quantity FROM products WHERE id = ?", arrayOf(productId.toString()))
            val currentQty = cursor.use {
                if (it.moveToNext()) it.getInt(0) else return@withContext false
            }

            val newQty = when (type) {
                StockMovementType.IN -> currentQty + delta
                StockMovementType.OUT, StockMovementType.SALE -> {
                    val computed = currentQty - delta
                    if (computed < 0) return@withContext false // Cannot reduce stock below 0
                    computed
                }
                StockMovementType.ADJUST -> delta // delta is exact new count
            }

            val effectiveDelta = when (type) {
                StockMovementType.IN -> delta
                StockMovementType.OUT, StockMovementType.SALE -> -delta
                StockMovementType.ADJUST -> delta - currentQty
            }

            val now = currentTimestamp()
            val prodCv = ContentValues().apply {
                put("quantity", newQty)
                put("updated_at", now)
            }
            db.update("products", prodCv, "id = ?", arrayOf(productId.toString()))

            val movCv = ContentValues().apply {
                put("product_id", productId)
                put("type", type.name)
                put("delta", effectiveDelta)
                put("reason", reason.ifBlank { "${type.name} adjustment" })
                put("created_at", now)
            }
            db.insert("stock_movements", null, movCv)

            db.setTransactionSuccessful()
            dbHelper.notifyUpdated()
            true
        } finally {
            db.endTransaction()
        }
    }

    override suspend fun getStockMovements(productId: Long?, limit: Int): List<StockMovement> = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        val list = mutableListOf<StockMovement>()
        val query = if (productId != null) {
            "SELECT * FROM stock_movements WHERE product_id = ? ORDER BY id DESC LIMIT ?"
        } else {
            "SELECT * FROM stock_movements ORDER BY id DESC LIMIT ?"
        }
        val args = if (productId != null) arrayOf(productId.toString(), limit.toString()) else arrayOf(limit.toString())
        val cursor = db.rawQuery(query, args)
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    StockMovement(
                        id = it.getLong(it.getColumnIndexOrThrow("id")),
                        productId = it.getLong(it.getColumnIndexOrThrow("product_id")),
                        type = StockMovementType.valueOf(it.getString(it.getColumnIndexOrThrow("type"))),
                        delta = it.getInt(it.getColumnIndexOrThrow("delta")),
                        reason = it.getString(it.getColumnIndexOrThrow("reason")),
                        createdAt = it.getString(it.getColumnIndexOrThrow("created_at"))
                    )
                )
            }
        }
        list
    }

    override suspend fun generateNextQrCode(): String = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        var maxNum = 0
        val cursor = db.rawQuery("SELECT qr_code FROM products", null)
        cursor.use {
            while (it.moveToNext()) {
                val code = it.getString(0)
                if (code.startsWith("TT-")) {
                    val num = code.removePrefix("TT-").toIntOrNull() ?: 0
                    if (num > maxNum) maxNum = num
                }
            }
        }
        "TT-%06d".format(maxNum + 1)
    }

    override suspend fun getAllCategories(): List<String> = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        val list = mutableListOf<String>()
        val cursor = db.rawQuery("SELECT DISTINCT category FROM products ORDER BY category ASC", null)
        cursor.use {
            while (it.moveToNext()) {
                list.add(it.getString(0))
            }
        }
        list
    }

    override suspend fun getAllOwners(): List<String> = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        val list = mutableListOf<String>()
        val cursor = db.rawQuery("SELECT DISTINCT owner FROM products ORDER BY owner ASC", null)
        cursor.use {
            while (it.moveToNext()) {
                list.add(it.getString(0))
            }
        }
        list
    }
}
