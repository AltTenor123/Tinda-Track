package com.example.data.seeder

import android.content.ContentValues
import com.example.data.db.TindaDatabaseHelper
import com.example.domain.model.StockMovementType
import com.example.domain.model.UtangStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object SampleDataSeeder {

    suspend fun seedIfEmpty(dbHelper: TindaDatabaseHelper) = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM products", null)
        val count = cursor.use { if (it.moveToNext()) it.getInt(0) else 0 }
        if (count == 0) {
            restoreSampleData(dbHelper)
        }
    }

    suspend fun restoreSampleData(dbHelper: TindaDatabaseHelper) = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            // Clean tables
            db.execSQL("DELETE FROM sale_items;")
            db.execSQL("DELETE FROM sales;")
            db.execSQL("DELETE FROM stock_movements;")
            db.execSQL("DELETE FROM products;")
            db.execSQL("DELETE FROM utang_payments;")
            db.execSQL("DELETE FROM utang;")

            // 1. Seed 35 Realistic Sari-Sari Store Products
            val products = listOf(
                ProductSeed("Lucky Me Pancit Canton Original", "Ate Susan", "Instant Noodles", "Monde Nissin Direct", "LMPC-ORIG", "TT-000001", 1350L, 1600L, 34, 10, "2027-04-15", "🍜"),
                ProductSeed("Lucky Me Pancit Canton Kalamansi", "Ate Susan", "Instant Noodles", "Monde Nissin Direct", "LMPC-KAL", "TT-000002", 1350L, 1600L, 8, 12, "2027-04-15", "🍜"),
                ProductSeed("Lucky Me Pancit Canton Chilimansi", "Ate Susan", "Instant Noodles", "Monde Nissin Direct", "LMPC-CHILI", "TT-000003", 1350L, 1600L, 28, 10, "2027-04-15", "🍜"),
                ProductSeed("Lucky Me Beef Mami", "Nanay Cora", "Instant Noodles", "Monde Nissin Direct", "LMB-MAMI", "TT-000004", 1200L, 1500L, 0, 10, "2027-03-20", "🍜"),
                ProductSeed("Coca-Cola Mismo 290ml", "Kuya Bert", "Beverages", "San Miguel Dist.", "COKE-MISMO", "TT-000005", 1600L, 2000L, 24, 12, "2027-06-30", "🥤"),
                ProductSeed("Sprite Mismo 290ml", "Kuya Bert", "Beverages", "San Miguel Dist.", "SPRITE-MISMO", "TT-000006", 1600L, 2000L, 15, 8, "2027-06-30", "🥤"),
                ProductSeed("Royal Tru-Orange Mismo 290ml", "Kuya Bert", "Beverages", "San Miguel Dist.", "ROYAL-MISMO", "TT-000007", 1600L, 2000L, 6, 8, "2027-06-30", "🥤"),
                ProductSeed("Kopiko Blanca 3-in-1 Coffee Twin", "Nanay Cora", "Coffee & Tea", "Puregold Wholesale", "KOPIKO-BLN", "TT-000008", 1150L, 1400L, 45, 15, "2026-11-20", "☕"),
                ProductSeed("Nescafe Classic 3-in-1 Original", "Nanay Cora", "Coffee & Tea", "Nestle Distribution", "NES-3IN1", "TT-000009", 1100L, 1350L, 32, 15, "2027-05-10", "☕"),
                ProductSeed("Great Taste White Twin Pack", "Nanay Cora", "Coffee & Tea", "Universal Robina Corp", "GT-WHITE", "TT-000010", 1150L, 1400L, 5, 15, "2026-11-28", "☕"),
                ProductSeed("Safeguard Pure White Bar Soap 60g", "Ate Susan", "Personal Care", "P&G Local Supply", "SAFEGUARD-WHT", "TT-000011", 2100L, 2600L, 18, 6, "2028-01-01", "🧼"),
                ProductSeed("Palmolive Shampoo Sachet Green 15ml", "Ate Susan", "Personal Care", "Puregold Wholesale", "PALM-GRN", "TT-000012", 600L, 800L, 50, 20, "2027-08-15", "🧴"),
                ProductSeed("Sunsilk Shampoo Pink Sachet 15ml", "Ate Susan", "Personal Care", "Unilever Phils", "SUNSILK-PNK", "TT-000013", 600L, 800L, 42, 20, "2027-08-15", "🧴"),
                ProductSeed("Colgate Max Cavity Protection 25g", "Ate Susan", "Personal Care", "Puregold Wholesale", "COLGATE-25G", "TT-000014", 1800L, 2300L, 12, 5, "2027-10-10", "🪥"),
                ProductSeed("Argentina Corned Beef 150g", "Kuya Bert", "Canned Goods", "Puregold Wholesale", "ARG-CB-150", "TT-000015", 3200L, 3800L, 16, 6, "2027-12-31", "🥫"),
                ProductSeed("Mega Sardines Tomato Sauce Red 155g", "Kuya Bert", "Canned Goods", "Puregold Wholesale", "MEGA-RED-155", "TT-000016", 2200L, 2600L, 22, 8, "2027-11-15", "🥫"),
                ProductSeed("555 Tuna Flakes in Oil 155g", "Kuya Bert", "Canned Goods", "Puregold Wholesale", "555-TUNA-OIL", "TT-000017", 2700L, 3300L, 3, 6, "2027-10-01", "🥫"),
                ProductSeed("CDO Karne Norte 150g", "Kuya Bert", "Canned Goods", "Puregold Wholesale", "CDO-KN-150", "TT-000018", 2400L, 2900L, 14, 6, "2026-11-18", "🥫"),
                ProductSeed("Piattos Cheese 85g", "Nanay Cora", "Snacks & Chips", "Universal Robina Corp", "PIATTOS-CHS", "TT-000019", 2900L, 3500L, 10, 5, "2027-02-14", "🥔"),
                ProductSeed("Nova Multigrain Country Cheddar 78g", "Nanay Cora", "Snacks & Chips", "Universal Robina Corp", "NOVA-CHD", "TT-000020", 2900L, 3500L, 0, 5, "2027-03-01", "🌾"),
                ProductSeed("Chippy Barbecue 110g", "Nanay Cora", "Snacks & Chips", "Universal Robina Corp", "CHIPPY-BBQ", "TT-000021", 2400L, 2900L, 15, 5, "2027-04-10", "🌽"),
                ProductSeed("Oishi Prawn Crackers Plain 50g", "Nanay Cora", "Snacks & Chips", "Puregold Wholesale", "OISHI-PRN", "TT-000022", 1400L, 1800L, 16, 6, "2026-11-25", "🦐"),
                ProductSeed("SkyFlakes Crackers Single Pack", "Ate Susan", "Biscuits & Bakery", "Monde Nissin Direct", "SKYFLAKES-SNGL", "TT-000023", 650L, 850L, 60, 20, "2027-05-01", "🍘"),
                ProductSeed("Fita Biscuits Small Roll", "Ate Susan", "Biscuits & Bakery", "Monde Nissin Direct", "FITA-ROLL", "TT-000024", 900L, 1200L, 25, 10, "2027-04-20", "🍘"),
                ProductSeed("Rebisco Sandwich Choco", "Ate Susan", "Biscuits & Bakery", "Puregold Wholesale", "REBISCO-CHOC", "TT-000025", 700L, 900L, 30, 10, "2027-03-15", "🥪"),
                ProductSeed("Silver Swan Soy Sauce 200ml Pouch", "Kuya Bert", "Condiments & Cooking", "Puregold Wholesale", "SS-TOYO-200", "TT-000026", 1100L, 1400L, 14, 6, "2027-09-01", "🍾"),
                ProductSeed("Datu Puti Vinegar 200ml Pouch", "Kuya Bert", "Condiments & Cooking", "Puregold Wholesale", "DP-SUKA-200", "TT-000027", 950L, 1200L, 16, 6, "2027-09-01", "🍾"),
                ProductSeed("Magic Sarap 8g Sachet", "Kuya Bert", "Condiments & Cooking", "Nestle Distribution", "MAGIC-SARAP", "TT-000028", 450L, 600L, 80, 25, "2027-07-20", "🧂"),
                ProductSeed("Golden Fiesta Cooking Oil 200ml", "Kuya Bert", "Condiments & Cooking", "Puregold Wholesale", "GF-OIL-200", "TT-000029", 2200L, 2800L, 11, 5, "2027-06-15", "🫗"),
                ProductSeed("Surf Powder Sun Fresh 50g Sachet", "Nanay Cora", "Laundry & Cleaning", "Unilever Phils", "SURF-50G", "TT-000030", 700L, 950L, 40, 15, null, "🫧"),
                ProductSeed("Downy Sunrise Fresh 20ml Sachet", "Nanay Cora", "Laundry & Cleaning", "P&G Local Supply", "DOWNY-SUN", "TT-000031", 750L, 1000L, 35, 15, null, "🌸"),
                ProductSeed("Bear Brand Fortified Milk 33g", "Ate Susan", "Dairy", "Nestle Distribution", "BEAR-MILK-33", "TT-000032", 1200L, 1500L, 24, 10, "2027-01-10", "🥛"),
                ProductSeed("Alaska Evaporada 370ml Can", "Ate Susan", "Dairy", "Puregold Wholesale", "ALASKA-EVAP", "TT-000033", 2800L, 3400L, 8, 5, "2027-02-28", "🥛"),
                ProductSeed("Maxx Menthol Candy 50s Pack", "Kuya Bert", "Candies & Sweets", "Universal Robina Corp", "MAXX-MNT-50", "TT-000034", 4000L, 5000L, 4, 5, "2027-11-30", "🍬"),
                ProductSeed("Choc-Nut Peanut Chocolate", "Kuya Bert", "Candies & Sweets", "Puregold Wholesale", "CHOCNUT-PC", "TT-000035", 200L, 300L, 100, 30, "2026-11-30", "🍫")
            )

            val insertedProductIds = mutableListOf<Long>()
            val productMap = mutableMapOf<Long, ProductSeed>()

            for (p in products) {
                val cv = ContentValues().apply {
                    put("name", p.name)
                    put("owner", p.owner)
                    put("category", p.category)
                    put("supplier", p.supplier)
                    put("sku", p.sku)
                    put("qr_code", p.qrCode)
                    put("cost_price", p.costPrice)
                    put("selling_price", p.sellingPrice)
                    put("quantity", p.quantity)
                    put("reorder_level", p.reorderLevel)
                    put("expiry_date", p.expiryDate)
                    put("photo_path", p.emoji) // Store emoji tag for fallback visual icon
                    put("created_at", "2026-10-20 08:00:00")
                    put("updated_at", "2026-10-20 08:00:00")
                }
                val id = db.insert("products", null, cv)
                insertedProductIds.add(id)
                productMap[id] = p

                // Movement
                val movCv = ContentValues().apply {
                    put("product_id", id)
                    put("type", StockMovementType.IN.name)
                    put("delta", p.quantity)
                    put("reason", "Opening Stock Seeder")
                    put("created_at", "2026-10-20 08:00:00")
                }
                db.insert("stock_movements", null, movCv)
            }

            // 2. Seed 50 Realistic Sales across the last 14 days (Oct 24 - Nov 6, 2026)
            val dates = (24..31).map { "2026-10-$it" } + (1..6).map { "2026-11-%02d".format(it) }
            var saleCounter = 1

            for ((dayIndex, dateStr) in dates.withIndex()) {
                val salesToday = if (dayIndex % 3 == 0) 4 else 3 // 3-4 sales per day = ~50 sales
                for (s in 1..salesToday) {
                    val hour = 7 + (s * 3) + (dayIndex % 2)
                    val minute = (s * 13 + dayIndex * 7) % 60
                    val timeStr = "%02d:%02d:00".format(hour.coerceAtMost(21), minute)
                    val createdAt = "$dateStr $timeStr"
                    val receiptNo = "RCP-26%02d-%04d".format(dayIndex + 1, saleCounter++)

                    // Pick 1 to 3 items
                    val itemIndex1 = (saleCounter * 3) % insertedProductIds.size
                    val itemIndex2 = (saleCounter * 7) % insertedProductIds.size
                    val p1Id = insertedProductIds[itemIndex1]
                    val p1 = productMap[p1Id]!!
                    val q1 = if (p1.sellingPrice < 1000L) 3 else 1

                    val p2Id = insertedProductIds[itemIndex2]
                    val p2 = productMap[p2Id]!!
                    val q2 = 1

                    val total = (p1.sellingPrice * q1) + (p2.sellingPrice * q2)
                    val totalCost = (p1.costPrice * q1) + (p2.costPrice * q2)
                    val profit = total - totalCost
                    val cashReceived = ((total + 999L) / 1000L) * 1000L // rounded up to next 10 or 50 pesos
                    val change = cashReceived - total

                    val saleCv = ContentValues().apply {
                        put("receipt_no", receiptNo)
                        put("created_at", createdAt)
                        put("cashier", if (dayIndex % 2 == 0) "Ate Susan" else "Kuya Bert")
                        put("total", total)
                        put("cash_received", cashReceived)
                        put("change", change)
                        put("estimated_profit", profit)
                    }
                    val saleId = db.insert("sales", null, saleCv)

                    // Item 1
                    val item1Cv = ContentValues().apply {
                        put("sale_id", saleId)
                        put("product_id", p1Id)
                        put("product_name", p1.name)
                        put("quantity", q1)
                        put("unit_price", p1.sellingPrice)
                        put("unit_cost", p1.costPrice)
                    }
                    db.insert("sale_items", null, item1Cv)

                    // Item 2
                    val item2Cv = ContentValues().apply {
                        put("sale_id", saleId)
                        put("product_id", p2Id)
                        put("product_name", p2.name)
                        put("quantity", q2)
                        put("unit_price", p2.sellingPrice)
                        put("unit_cost", p2.costPrice)
                    }
                    db.insert("sale_items", null, item2Cv)
                }
            }

            // 3. Seed 5 Utang (Customer Credit) Records with partial payments
            val utangRecords = listOf(
                UtangSeed(
                    customer = "Aling Marites",
                    items = "2x Pancit Canton Chilimansi, 1x Coke Mismo, 1x Magic Sarap",
                    amount = 5800L, // ₱58.00
                    paid = 0L,
                    status = UtangStatus.Unpaid,
                    createdAt = "2026-11-04 17:30:00",
                    payments = emptyList()
                ),
                UtangSeed(
                    customer = "Mang Kanor",
                    items = "3x Kopiko Blanca Twin, 1x SkyFlakes, 1x Safeguard White",
                    amount = 7650L, // ₱76.50
                    paid = 5000L, // ₱50.00
                    status = UtangStatus.Partial,
                    createdAt = "2026-11-02 09:15:00",
                    payments = listOf(Pair(5000L, "2026-11-05 16:00:00"))
                ),
                UtangSeed(
                    customer = "Ate Gina (Tupad Kapitbahay)",
                    items = "1x Argentina Corned Beef, 1x Datu Puti Suka, 2x Bear Brand",
                    amount = 8000L, // ₱80.00
                    paid = 8000L, // ₱80.00
                    status = UtangStatus.Paid,
                    createdAt = "2026-10-28 11:20:00",
                    payments = listOf(
                        Pair(4000L, "2026-10-30 18:00:00"),
                        Pair(4000L, "2026-11-03 14:30:00")
                    )
                ),
                UtangSeed(
                    customer = "Kuya Jojo Tricycle Driver",
                    items = "1x Royal Mismo, 1x Piattos Cheese, 1x Fita Roll",
                    amount = 6700L, // ₱67.00
                    paid = 2000L, // ₱20.00
                    status = UtangStatus.Partial,
                    createdAt = "2026-11-05 13:40:00",
                    payments = listOf(Pair(2000L, "2026-11-06 18:10:00"))
                ),
                UtangSeed(
                    customer = "Nanay Baby (Kanto Sari-sari)",
                    items = "5x Surf Powder Sun Fresh, 5x Downy Sunrise Fresh",
                    amount = 9750L, // ₱97.50
                    paid = 0L,
                    status = UtangStatus.Unpaid,
                    createdAt = "2026-11-06 08:30:00",
                    payments = emptyList()
                )
            )

            for (u in utangRecords) {
                val utangCv = ContentValues().apply {
                    put("customer_name", u.customer)
                    put("items_note", u.items)
                    put("amount", u.amount)
                    put("paid", u.paid)
                    put("status", u.status.name)
                    put("created_at", u.createdAt)
                    put("updated_at", u.createdAt)
                }
                val utangId = db.insert("utang", null, utangCv)

                for ((payAmt, payDate) in u.payments) {
                    val payCv = ContentValues().apply {
                        put("utang_id", utangId)
                        put("amount", payAmt)
                        put("created_at", payDate)
                    }
                    db.insert("utang_payments", null, payCv)
                }
            }

            db.setTransactionSuccessful()
            dbHelper.notifyUpdated()
        } finally {
            db.endTransaction()
        }
    }

    private data class ProductSeed(
        val name: String,
        val owner: String,
        val category: String,
        val supplier: String,
        val sku: String,
        val qrCode: String,
        val costPrice: Long,
        val sellingPrice: Long,
        val quantity: Int,
        val reorderLevel: Int,
        val expiryDate: String?,
        val emoji: String
    )

    private data class UtangSeed(
        val customer: String,
        val items: String,
        val amount: Long,
        val paid: Long,
        val status: UtangStatus,
        val createdAt: String,
        val payments: List<Pair<Long, String>>
    )
}
