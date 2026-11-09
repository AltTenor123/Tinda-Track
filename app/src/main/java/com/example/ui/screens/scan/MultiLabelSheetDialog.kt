package com.example.ui.screens.scan

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import com.example.domain.model.CurrencyHelper
import com.example.domain.model.Product
import com.example.platform.QrCodeGenerator
import com.example.ui.components.TindaOutlinedButton
import com.example.ui.components.TindaPrimaryButton
import com.example.ui.theme.*
import java.io.File
import java.io.FileOutputStream

@Composable
fun MultiLabelSheetDialog(
    products: List<Product>,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var isSharing by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(12.dp)
                .testTag("multi_label_sheet_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = CardCream,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Printable Label Sheet",
                            style = TindaTypography.titleMedium,
                            color = Ink
                        )
                        Text(
                            text = "${products.size} product tags ready for shelf display",
                            style = TindaTypography.bodySmall,
                            color = Muted
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Muted)
                    }
                }

                // Grid of labels preview
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
                        .background(androidx.compose.ui.graphics.Color.White, RoundedCornerShape(16.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(products) { product ->
                        val qrBmp = remember(product.qrCode) {
                            QrCodeGenerator.generateQrImageBitmap(product.qrCode, 150)
                        }

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, CardBorder, RoundedCornerShape(10.dp)),
                            shape = RoundedCornerShape(10.dp),
                            color = CreamLight
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Image(
                                    bitmap = qrBmp,
                                    contentDescription = product.name,
                                    modifier = Modifier.size(70.dp)
                                )
                                Text(
                                    text = product.name,
                                    style = TindaTypography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Ink,
                                    maxLines = 1,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = CurrencyHelper.formatCentavos(product.sellingPrice),
                                    style = TindaTypography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Forest
                                )
                                Text(
                                    text = product.qrCode,
                                    fontSize = 9.sp,
                                    color = Muted
                                )
                            }
                        }
                    }
                }

                // Bottom actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TindaOutlinedButton(
                        text = "Close",
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    )

                    TindaPrimaryButton(
                        text = if (isSharing) "Preparing..." else "Share Sheet",
                        icon = Icons.Default.Share,
                        enabled = !isSharing && products.isNotEmpty(),
                        onClick = {
                            isSharing = true
                            shareMultiLabelSheet(context, products)
                            isSharing = false
                        },
                        modifier = Modifier.weight(1.3f)
                    )
                }
            }
        }
    }
}

private fun shareMultiLabelSheet(context: Context, products: List<Product>) {
    try {
        val sheetWidth = 1200
        val labelWidth = 360
        val labelHeight = 440
        val margin = 40
        val padding = 20
        val cols = 3
        val rows = (products.size + cols - 1) / cols
        val sheetHeight = margin * 2 + rows * (labelHeight + padding)

        val bitmap = Bitmap.createBitmap(sheetWidth, sheetHeight.coerceAtLeast(800), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(android.graphics.Color.WHITE)

        val borderPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#E5DFD1")
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
        val textPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#14271D")
            textSize = 20f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        val pricePaint = Paint().apply {
            color = android.graphics.Color.parseColor("#0E2F24")
            textSize = 26f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        val codePaint = Paint().apply {
            color = android.graphics.Color.parseColor("#69776B")
            textSize = 15f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }

        products.forEachIndexed { index, product ->
            val col = index % cols
            val row = index / cols
            val left = margin + col * (labelWidth + padding)
            val top = margin + row * (labelHeight + padding)
            val right = left + labelWidth
            val bottom = top + labelHeight

            // Label border
            canvas.drawRect(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), borderPaint)

            // QR Code
            val qr = QrCodeGenerator.generateQrBitmap(product.qrCode, 200)
            canvas.drawBitmap(qr, left + (labelWidth - 200) / 2f, top + 20f, null)

            // Product text
            val truncated = if (product.name.length > 20) product.name.take(19) + "…" else product.name
            canvas.drawText(truncated, left + labelWidth / 2f, top + 260f, textPaint)
            canvas.drawText(CurrencyHelper.formatCentavos(product.sellingPrice), left + labelWidth / 2f, top + 310f, pricePaint)
            canvas.drawText(product.qrCode, left + labelWidth / 2f, top + 350f, codePaint)
            canvas.drawText(product.category, left + labelWidth / 2f, top + 380f, codePaint)
        }

        val cacheFolder = File(context.cacheDir, "qr_labels")
        cacheFolder.mkdirs()
        val file = File(cacheFolder, "tinda_track_multi_labels.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Tinda Track Printable Labels (${products.size} items)")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Printable Multi-Label Sheet"))
    } catch (_: Exception) {}
}
