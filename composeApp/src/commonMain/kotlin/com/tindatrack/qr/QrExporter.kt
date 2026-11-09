package com.tindatrack.qr

import androidx.compose.ui.graphics.ImageBitmap

/**
 * Multiplatform QR generator and label exporter.
 */
expect object QrExporter {
    fun generateQrBitmap(data: String, sizePx: Int = 400): ImageBitmap
    fun exportLabelPng(qrCode: String, name: String, priceText: String): ByteArray
}
