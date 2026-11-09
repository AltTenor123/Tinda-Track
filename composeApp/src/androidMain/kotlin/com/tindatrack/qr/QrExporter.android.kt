package com.tindatrack.qr

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import qrcode.QRCode
import qrcode.color.Colors
import java.io.ByteArrayOutputStream

actual object QrExporter {
    actual fun generateQrBitmap(data: String, sizePx: Int): ImageBitmap {
        val qr = QRCode.ofSquares()
            .withSize(sizePx)
            .withColor(Colors.css("#0E2F24"))
            .withBackgroundColor(Colors.css("#FFFCF6"))
            .build(data)
        val rawBytes = qr.renderToBytes()
        val bmp = android.graphics.BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.size)
        return bmp.asImageBitmap()
    }

    actual fun exportLabelPng(qrCode: String, name: String, priceText: String): ByteArray {
        val width = 500
        val height = 650
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

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

        // Draw QR
        val qr = QRCode.ofSquares()
            .withSize(320)
            .withColor(Colors.css("#0E2F24"))
            .withBackgroundColor(Colors.css("#FFFCF6"))
            .build(qrCode)
        val rawBytes = qr.renderToBytes()
        val qrBmp = android.graphics.BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.size)
        canvas.drawBitmap(qrBmp, (width - 320) / 2f, 85f, null)

        val namePaint = Paint().apply {
            color = android.graphics.Color.parseColor("#14271D")
            textSize = 28f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText(name.take(24), width / 2f, 450f, namePaint)

        val pricePaint = Paint().apply {
            color = android.graphics.Color.parseColor("#0E2F24")
            textSize = 42f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText(priceText, width / 2f, 515f, pricePaint)

        val out = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        return out.toByteArray()
    }
}
