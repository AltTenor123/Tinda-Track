package com.example.data.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.domain.model.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class TindaDatabaseHelper(context: Context) : SQLiteOpenHelper(
    context,
    DATABASE_NAME,
    null,
    DATABASE_VERSION
) {
    companion object {
        const val DATABASE_NAME = "tinda_track.db"
        const val DATABASE_VERSION = 1

        @Volatile
        private var instance: TindaDatabaseHelper? = null

        fun getInstance(context: Context): TindaDatabaseHelper {
            return instance ?: synchronized(this) {
                instance ?: TindaDatabaseHelper(context.applicationContext).also { instance = it }
            }
        }
    }

    private val _databaseUpdates = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val databaseUpdates: SharedFlow<Unit> = _databaseUpdates.asSharedFlow()

    fun notifyUpdated() {
        _databaseUpdates.tryEmit(Unit)
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE products (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                owner TEXT NOT NULL,
                category TEXT NOT NULL,
                supplier TEXT NOT NULL,
                sku TEXT NOT NULL,
                qr_code TEXT NOT NULL UNIQUE,
                cost_price INTEGER NOT NULL,
                selling_price INTEGER NOT NULL,
                quantity INTEGER NOT NULL,
                reorder_level INTEGER NOT NULL,
                expiry_date TEXT,
                photo_path TEXT,
                created_at TEXT NOT NULL,
                updated_at TEXT NOT NULL
            );
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE stock_movements (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                product_id INTEGER NOT NULL,
                type TEXT NOT NULL,
                delta INTEGER NOT NULL,
                reason TEXT NOT NULL,
                created_at TEXT NOT NULL,
                FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
            );
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE sales (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                receipt_no TEXT NOT NULL UNIQUE,
                created_at TEXT NOT NULL,
                cashier TEXT NOT NULL,
                total INTEGER NOT NULL,
                cash_received INTEGER NOT NULL,
                change INTEGER NOT NULL,
                estimated_profit INTEGER NOT NULL
            );
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE sale_items (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                sale_id INTEGER NOT NULL,
                product_id INTEGER NOT NULL,
                product_name TEXT NOT NULL,
                quantity INTEGER NOT NULL,
                unit_price INTEGER NOT NULL,
                unit_cost INTEGER NOT NULL,
                FOREIGN KEY (sale_id) REFERENCES sales(id) ON DELETE CASCADE
            );
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE utang (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                customer_name TEXT NOT NULL,
                items_note TEXT NOT NULL,
                amount INTEGER NOT NULL,
                paid INTEGER NOT NULL,
                status TEXT NOT NULL,
                created_at TEXT NOT NULL,
                updated_at TEXT NOT NULL
            );
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE utang_payments (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                utang_id INTEGER NOT NULL,
                amount INTEGER NOT NULL,
                created_at TEXT NOT NULL,
                FOREIGN KEY (utang_id) REFERENCES utang(id) ON DELETE CASCADE
            );
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE settings (
                key TEXT PRIMARY KEY,
                value TEXT NOT NULL
            );
            """.trimIndent()
        )

        // Create indexes for fast lookup and barcode scanning
        db.execSQL("CREATE INDEX idx_products_qr ON products(qr_code);")
        db.execSQL("CREATE INDEX idx_products_sku ON products(sku);")
        db.execSQL("CREATE INDEX idx_sales_receipt ON sales(receipt_no);")
        db.execSQL("CREATE INDEX idx_sales_created ON sales(created_at);")
        db.execSQL("CREATE INDEX idx_utang_status ON utang(status);")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Migration strategy for future versions
    }

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    // Helper functions for mapping cursors to models
    fun cursorToProduct(cursor: Cursor): Product {
        return Product(
            id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
            name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
            owner = cursor.getString(cursor.getColumnIndexOrThrow("owner")),
            category = cursor.getString(cursor.getColumnIndexOrThrow("category")),
            supplier = cursor.getString(cursor.getColumnIndexOrThrow("supplier")),
            sku = cursor.getString(cursor.getColumnIndexOrThrow("sku")),
            qrCode = cursor.getString(cursor.getColumnIndexOrThrow("qr_code")),
            costPrice = cursor.getLong(cursor.getColumnIndexOrThrow("cost_price")),
            sellingPrice = cursor.getLong(cursor.getColumnIndexOrThrow("selling_price")),
            quantity = cursor.getInt(cursor.getColumnIndexOrThrow("quantity")),
            reorderLevel = cursor.getInt(cursor.getColumnIndexOrThrow("reorder_level")),
            expiryDate = if (cursor.isNull(cursor.getColumnIndexOrThrow("expiry_date"))) null else cursor.getString(cursor.getColumnIndexOrThrow("expiry_date")),
            photoPath = if (cursor.isNull(cursor.getColumnIndexOrThrow("photo_path"))) null else cursor.getString(cursor.getColumnIndexOrThrow("photo_path")),
            createdAt = cursor.getString(cursor.getColumnIndexOrThrow("created_at")),
            updatedAt = cursor.getString(cursor.getColumnIndexOrThrow("updated_at"))
        )
    }

    fun cursorToSale(cursor: Cursor, items: List<SaleItem> = emptyList()): Sale {
        return Sale(
            id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
            receiptNo = cursor.getString(cursor.getColumnIndexOrThrow("receipt_no")),
            createdAt = cursor.getString(cursor.getColumnIndexOrThrow("created_at")),
            cashier = cursor.getString(cursor.getColumnIndexOrThrow("cashier")),
            total = cursor.getLong(cursor.getColumnIndexOrThrow("total")),
            cashReceived = cursor.getLong(cursor.getColumnIndexOrThrow("cash_received")),
            change = cursor.getLong(cursor.getColumnIndexOrThrow("change")),
            estimatedProfit = cursor.getLong(cursor.getColumnIndexOrThrow("estimated_profit")),
            items = items
        )
    }

    fun cursorToSaleItem(cursor: Cursor): SaleItem {
        return SaleItem(
            id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
            saleId = cursor.getLong(cursor.getColumnIndexOrThrow("sale_id")),
            productId = cursor.getLong(cursor.getColumnIndexOrThrow("product_id")),
            productName = cursor.getString(cursor.getColumnIndexOrThrow("product_name")),
            quantity = cursor.getInt(cursor.getColumnIndexOrThrow("quantity")),
            unitPrice = cursor.getLong(cursor.getColumnIndexOrThrow("unit_price")),
            unitCost = cursor.getLong(cursor.getColumnIndexOrThrow("unit_cost"))
        )
    }

    fun cursorToUtang(cursor: Cursor, payments: List<UtangPayment> = emptyList()): Utang {
        val statusStr = cursor.getString(cursor.getColumnIndexOrThrow("status"))
        val status = try {
            UtangStatus.valueOf(statusStr)
        } catch (_: Exception) {
            UtangStatus.Unpaid
        }
        return Utang(
            id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
            customerName = cursor.getString(cursor.getColumnIndexOrThrow("customer_name")),
            itemsNote = cursor.getString(cursor.getColumnIndexOrThrow("items_note")),
            amount = cursor.getLong(cursor.getColumnIndexOrThrow("amount")),
            paid = cursor.getLong(cursor.getColumnIndexOrThrow("paid")),
            status = status,
            createdAt = cursor.getString(cursor.getColumnIndexOrThrow("created_at")),
            updatedAt = cursor.getString(cursor.getColumnIndexOrThrow("updated_at")),
            payments = payments
        )
    }

    fun cursorToUtangPayment(cursor: Cursor): UtangPayment {
        return UtangPayment(
            id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
            utangId = cursor.getLong(cursor.getColumnIndexOrThrow("utang_id")),
            amount = cursor.getLong(cursor.getColumnIndexOrThrow("amount")),
            createdAt = cursor.getString(cursor.getColumnIndexOrThrow("created_at"))
        )
    }
}
