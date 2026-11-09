package com.example.data.repository

import android.content.ContentValues
import com.example.data.db.TindaDatabaseHelper
import com.example.domain.model.Utang
import com.example.domain.model.UtangPayment
import com.example.domain.model.UtangStatus
import com.example.domain.repository.UtangRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class UtangRepositoryImpl(
    private val dbHelper: TindaDatabaseHelper
) : UtangRepository {

    private val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
    private fun currentTimestamp(): String = dateTimeFormat.format(Date())

    override fun getAllUtangFlow(): Flow<List<Utang>> = flow {
        emit(getAllUtang())
        dbHelper.databaseUpdates.collect {
            emit(getAllUtang())
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun getAllUtang(): List<Utang> = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        val list = mutableListOf<Utang>()
        val cursor = db.rawQuery(
            """
            SELECT * FROM utang 
            ORDER BY 
                CASE status WHEN 'Unpaid' THEN 1 WHEN 'Partial' THEN 2 ELSE 3 END, 
                id DESC
            """.trimIndent(),
            null
        )
        cursor.use {
            while (it.moveToNext()) {
                val utang = dbHelper.cursorToUtang(it)
                val payments = getPaymentsForUtang(utang.id)
                list.add(utang.copy(payments = payments))
            }
        }
        list
    }

    override suspend fun getUtangById(id: Long): Utang? = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM utang WHERE id = ?", arrayOf(id.toString()))
        cursor.use {
            if (it.moveToNext()) {
                val utang = dbHelper.cursorToUtang(it)
                val payments = getPaymentsForUtang(utang.id)
                utang.copy(payments = payments)
            } else null
        }
    }

    override suspend fun createUtang(
        customerName: String,
        itemsNote: String,
        amount: Long
    ): Long = withContext(Dispatchers.IO) {
        if (customerName.isBlank()) throw IllegalArgumentException("Customer name cannot be empty")
        if (amount <= 0) throw IllegalArgumentException("Credit amount must be greater than zero")

        val now = currentTimestamp()
        val db = dbHelper.writableDatabase
        val cv = ContentValues().apply {
            put("customer_name", customerName.trim())
            put("items_note", itemsNote.trim())
            put("amount", amount)
            put("paid", 0L)
            put("status", UtangStatus.Unpaid.name)
            put("created_at", now)
            put("updated_at", now)
        }
        val id = db.insertOrThrow("utang", null, cv)
        if (id != -1L) {
            dbHelper.notifyUpdated()
        }
        id
    }

    override suspend fun recordPayment(utangId: Long, paymentAmount: Long): Result<Utang> = withContext(Dispatchers.IO) {
        if (paymentAmount <= 0) {
            return@withContext Result.failure(IllegalArgumentException("Payment amount must be greater than zero"))
        }

        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            val cursor = db.rawQuery(
                "SELECT customer_name, items_note, amount, paid, status, created_at FROM utang WHERE id = ?",
                arrayOf(utangId.toString())
            )

            if (!cursor.moveToNext()) {
                cursor.close()
                return@withContext Result.failure(IllegalArgumentException("Utang record not found"))
            }

            val customerName = cursor.getString(0)
            val itemsNote = cursor.getString(1)
            val totalAmount = cursor.getLong(2)
            val currentPaid = cursor.getLong(3)
            val createdAt = cursor.getString(5)
            cursor.close()

            val remainingBalance = totalAmount - currentPaid
            if (paymentAmount > remainingBalance) {
                return@withContext Result.failure(
                    IllegalArgumentException(
                        "Payment exceeds remaining balance. Max allowed: ₱${remainingBalance / 100.0}, entered: ₱${paymentAmount / 100.0}"
                    )
                )
            }

            val newPaid = currentPaid + paymentAmount
            val newStatus = when {
                newPaid >= totalAmount -> UtangStatus.Paid
                newPaid > 0 -> UtangStatus.Partial
                else -> UtangStatus.Unpaid
            }

            val now = currentTimestamp()

            // Update utang
            val cv = ContentValues().apply {
                put("paid", newPaid)
                put("status", newStatus.name)
                put("updated_at", now)
            }
            db.update("utang", cv, "id = ?", arrayOf(utangId.toString()))

            // Insert payment entry
            val payCv = ContentValues().apply {
                put("utang_id", utangId)
                put("amount", paymentAmount)
                put("created_at", now)
            }
            db.insert("utang_payments", null, payCv)

            db.setTransactionSuccessful()
            dbHelper.notifyUpdated()

            val updatedUtang = Utang(
                id = utangId,
                customerName = customerName,
                itemsNote = itemsNote,
                amount = totalAmount,
                paid = newPaid,
                status = newStatus,
                createdAt = createdAt,
                updatedAt = now,
                payments = getPaymentsForUtang(utangId)
            )
            Result.success(updatedUtang)
        } finally {
            db.endTransaction()
        }
    }

    override suspend fun deleteUtang(id: Long): Boolean = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        val affected = db.delete("utang", "id = ?", arrayOf(id.toString()))
        if (affected > 0) {
            dbHelper.notifyUpdated()
            true
        } else false
    }

    override suspend fun getOpenUtangTotal(): Long = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT SUM(amount - paid) FROM utang WHERE status != 'Paid'",
            null
        )
        cursor.use {
            if (it.moveToNext() && !it.isNull(0)) it.getLong(0) else 0L
        }
    }

    override suspend fun getPaymentsForUtang(utangId: Long): List<UtangPayment> {
        val db = dbHelper.readableDatabase
        val list = mutableListOf<UtangPayment>()
        val cursor = db.rawQuery("SELECT * FROM utang_payments WHERE utang_id = ? ORDER BY id DESC", arrayOf(utangId.toString()))
        cursor.use {
            while (it.moveToNext()) {
                list.add(dbHelper.cursorToUtangPayment(it))
            }
        }
        return list
    }
}
