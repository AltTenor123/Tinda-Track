package com.example.platform

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.net.Uri
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.content.FileProvider
import com.example.domain.model.CurrencyHelper
import com.example.domain.model.Product
import qrcode.QRCode
import qrcode.color.Colors
import java.io.File
import java.io.FileOutputStream

object QrCodeGenerator {

    /**
     * Generates a square QR code bitmap for a given string (e.g. "TT-000001").
     */
    fun generateQrBitmap(data: String, sizePx: Int = 400): Bitmap {
        val qr = QRCode.ofSquares()
            .withSize(sizePx)
            .withColor(Colors.css("#0E2F24"))
            .withBackgroundColor(Colors.css("#FFFCF6"))
            .build(data)

        // Native Android Bitmap
        val rawBytes = qr.renderToBytes()
        return android.graphics.BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.size)
    }

    fun generateQrImageBitmap(data: String, sizePx: Int = 400): ImageBitmap {
        val bmp = generateQrBitmap(data, sizePx)
        return bmp.asImageBitmap()
    }

    /**
     * Generates a printable price label bitmap with Store Name, Product Name, Price, and QR Code.
     */
    fun generateProductLabelBitmap(product: Product): Bitmap {
        val width = 500
        val height = 650
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Background
        canvas.drawColor(android.graphics.Color.WHITE)

        val borderPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#E5DFD1")
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }
        canvas.drawRect(8f, 8f, width - 8f, height - 8f, borderPaint)

        val headerPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#0E2F24")
            textSize = 24f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("TINDA TRACK STORE", width / 2f, 48f, headerPaint)

        val linePaint = Paint().apply {
            color = android.graphics.Color.parseColor("#0E2F24")
            strokeWidth = 2f
        }
        canvas.drawLine(40f, 65f, width - 40f, 65f, linePaint)

        // Draw QR Code in center
        val qrBmp = generateQrBitmap(product.qrCode, 320)
        canvas.drawBitmap(qrBmp, (width - 320) / 2f, 85f, null)

        // Product Name
        val namePaint = Paint().apply {
            color = android.graphics.Color.parseColor("#14271D")
            textSize = 28f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        val truncatedName = if (product.name.length > 25) product.name.take(24) + "…" else product.name
        canvas.drawText(truncatedName, width / 2f, 450f, namePaint)

        // Price
        val pricePaint = Paint().apply {
            color = android.graphics.Color.parseColor("#0E2F24")
            textSize = 42f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText(CurrencyHelper.formatCentavos(product.sellingPrice), width / 2f, 515f, pricePaint)

        // Code / Details footer
        val codePaint = Paint().apply {
            color = android.graphics.Color.parseColor("#69776B")
            textSize = 20f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("${product.qrCode} • SKU: ${product.sku}", width / 2f, 565f, codePaint)
        canvas.drawText("Owner: ${product.owner} • ${product.category}", width / 2f, 600f, codePaint)

        return bitmap
    }

    /**
     * Saves a bitmap as PNG in the cache directory and returns the content URI.
     */
    fun saveLabelToCache(context: Context, product: Product): Uri {
        val labelBitmap = generateProductLabelBitmap(product)
        val imagesFolder = File(context.cacheDir, "qr_labels")
        imagesFolder.mkdirs()
        val file = File(imagesFolder, "${product.qrCode}_label.png")
        FileOutputStream(file).use { out ->
            labelBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    /**
     * Shares the QR price label via Android Sharesheet.
     */
    fun shareProductLabel(context: Context, product: Product) {
        val uri = saveLabelToCache(context, product)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(
                Intent.EXTRA_SUBJECT,
                "QR Label: ${product.name} (${CurrencyHelper.formatCentavos(product.sellingPrice)})"
            )
            putExtra(
                Intent.EXTRA_TEXT,
                "${product.name} - ${CurrencyHelper.formatCentavos(product.sellingPrice)} [QR: ${product.qrCode}]"
            )
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share QR Price Label"))
    }
}
