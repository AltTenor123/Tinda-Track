package com.tindatrack.qr

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asComposeImageBitmap
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.Image
import qrcode.QRCode
import qrcode.color.Colors

actual object QrExporter {
    actual fun generateQrBitmap(data: String, sizePx: Int): ImageBitmap {
        val qr = QRCode.ofSquares()
            .withSize(sizePx)
            .withColor(Colors.css("#0E2F24"))
            .withBackgroundColor(Colors.css("#FFFCF6"))
            .build(data)
        val bytes = qr.renderToBytes()
        val skiaImage = Image.makeFromEncoded(bytes)
        return Bitmap.makeFromImage(skiaImage).asComposeImageBitmap()
    }

    actual fun exportLabelPng(qrCode: String, name: String, priceText: String): ByteArray {
        val qr = QRCode.ofSquares()
            .withSize(400)
            .withColor(Colors.css("#0E2F24"))
            .withBackgroundColor(Colors.css("#FFFFFF"))
            .build(qrCode)
        return qr.renderToBytes()
    }
}
