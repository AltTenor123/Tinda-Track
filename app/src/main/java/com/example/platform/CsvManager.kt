package com.example.platform

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.domain.model.*
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CsvManager {

    fun exportProductsToCsv(products: List<Product>): String {
        val sb = StringBuilder()
        sb.appendLine("id,name,owner,category,supplier,sku,qr_code,cost_price,selling_price,quantity,reorder_level,expiry_date,created_at,updated_at")
        for (p in products) {
            val name = escapeCsv(p.name)
            val owner = escapeCsv(p.owner)
            val cat = escapeCsv(p.category)
            val sup = escapeCsv(p.supplier)
            val sku = escapeCsv(p.sku)
            val qr = escapeCsv(p.qrCode)
            val exp = p.expiryDate ?: ""
            sb.appendLine("${p.id},$name,$owner,$cat,$sup,$sku,$qr,${p.costPrice},${p.sellingPrice},${p.quantity},${p.reorderLevel},$exp,${p.createdAt},${p.updatedAt}")
        }
        return sb.toString()
    }

    fun exportSalesToCsv(sales: List<Sale>): String {
        val sb = StringBuilder()
        sb.appendLine("id,receipt_no,created_at,cashier,total_centavos,cash_received_centavos,change_centavos,estimated_profit_centavos,items_summary")
        for (s in sales) {
            val itemsSummary = escapeCsv(s.items.joinToString("; ") { "${it.quantity}x ${it.productName}" })
            sb.appendLine("${s.id},${s.receiptNo},${s.createdAt},${escapeCsv(s.cashier)},${s.total},${s.cashReceived},${s.change},${s.estimatedProfit},$itemsSummary")
        }
        return sb.toString()
    }

    fun exportUtangToCsv(utangList: List<Utang>): String {
        val sb = StringBuilder()
        sb.appendLine("id,customer_name,items_note,amount_centavos,paid_centavos,remaining_balance_centavos,status,created_at,updated_at")
        for (u in utangList) {
            sb.appendLine("${u.id},${escapeCsv(u.customerName)},${escapeCsv(u.itemsNote)},${u.amount},${u.paid},${u.balance},${u.status.name},${u.createdAt},${u.updatedAt}")
        }
        return sb.toString()
    }

    fun parseProductsCsv(csvContent: String): List<Product> {
        val lines = csvContent.lines().filter { it.isNotBlank() }
        if (lines.size <= 1) return emptyList()

        val results = mutableListOf<Product>()
        val now = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).format(Date())

        for (i in 1 until lines.size) {
            val parts = parseCsvLine(lines[i])
            if (parts.size >= 10) {
                try {
                    val name = parts[1].trim()
                    if (name.isBlank()) continue
                    val product = Product(
                        id = parts[0].toLongOrNull() ?: 0L,
                        name = name,
                        owner = parts.getOrNull(2)?.ifBlank { "Nanay Rosa" } ?: "Nanay Rosa",
                        category = parts.getOrNull(3)?.ifBlank { "General" } ?: "General",
                        supplier = parts.getOrNull(4)?.ifBlank { "Local" } ?: "Local",
                        sku = parts.getOrNull(5)?.ifBlank { "SKU-${System.currentTimeMillis() % 100000}" } ?: "SKU-${System.currentTimeMillis() % 100000}",
                        qrCode = parts.getOrNull(6)?.ifBlank { "TT-${(100000..999999).random()}" } ?: "TT-${(100000..999999).random()}",
                        costPrice = parts.getOrNull(7)?.toLongOrNull() ?: 0L,
                        sellingPrice = parts.getOrNull(8)?.toLongOrNull() ?: 0L,
                        quantity = parts.getOrNull(9)?.toIntOrNull() ?: 0,
                        reorderLevel = parts.getOrNull(10)?.toIntOrNull() ?: 5,
                        expiryDate = parts.getOrNull(11)?.ifBlank { null },
                        createdAt = parts.getOrNull(12)?.ifBlank { now } ?: now,
                        updatedAt = now
                    )
                    results.add(product)
                } catch (_: Exception) {}
            }
        }
        return results
    }

    fun shareCsvFile(context: Context, filename: String, content: String, subject: String) {
        val cacheFolder = File(context.cacheDir, "exports")
        cacheFolder.mkdirs()
        val file = File(cacheFolder, filename)
        FileOutputStream(file).use { out ->
            out.write(content.toByteArray(Charsets.UTF_8))
        }

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, subject)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share $subject"))
    }

    private fun escapeCsv(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"" + value.replace("\"", "\"\"") + "\""
        } else {
            value
        }
    }

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        var curVal = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val c = line[i]
            if (inQuotes) {
                if (c == '\"') {
                    if (i + 1 < line.length && line[i + 1] == '\"') {
                        curVal.append('\"')
                        i++
                    } else {
                        inQuotes = false
                    }
                } else {
                    curVal.append(c)
                }
            } else {
                if (c == '\"') {
                    inQuotes = true
                } else if (c == ',') {
                    result.add(curVal.toString())
                    curVal = StringBuilder()
                } else {
                    curVal.append(c)
                }
            }
            i++
        }
        result.add(curVal.toString())
        return result
    }
}
