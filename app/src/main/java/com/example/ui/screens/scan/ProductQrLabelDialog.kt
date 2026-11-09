package com.example.ui.screens.scan

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Print
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
import com.example.domain.model.CurrencyHelper
import com.example.domain.model.Product
import com.example.platform.QrCodeGenerator
import com.example.ui.components.TindaOutlinedButton
import com.example.ui.components.TindaPrimaryButton
import com.example.ui.theme.*

@Composable
fun ProductQrLabelDialog(
    product: Product,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var labelBitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(product) {
        labelBitmap = QrCodeGenerator.generateProductLabelBitmap(product)
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("product_qr_label_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = CardCream,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header with close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Price Tag & QR Label",
                        style = TindaTypography.titleMedium,
                        color = Ink
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Muted
                        )
                    }
                }

                // Label Preview Card
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CardBorder, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    color = androidx.compose.ui.graphics.Color.White
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "TINDA TRACK STORE",
                            style = TindaTypography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Forest,
                            letterSpacing = 1.sp
                        )

                        HorizontalDivider(color = SageSoft, thickness = 1.dp)

                        // QR Code image
                        if (labelBitmap != null) {
                            Image(
                                bitmap = labelBitmap!!.asImageBitmap(),
                                contentDescription = "QR Code for ${product.name}",
                                modifier = Modifier
                                    .size(190.dp)
                                    .padding(4.dp)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(190.dp)
                                    .background(Cream),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = Forest, modifier = Modifier.size(32.dp))
                            }
                        }

                        Text(
                            text = product.name,
                            style = TindaTypography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Ink,
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )

                        Text(
                            text = CurrencyHelper.formatCentavos(product.sellingPrice),
                            style = TindaTypography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Forest
                        )

                        Text(
                            text = "${product.qrCode} • SKU: ${product.sku}",
                            style = TindaTypography.bodySmall,
                            color = Muted
                        )
                    }
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TindaOutlinedButton(
                        text = "Close",
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    )

                    TindaPrimaryButton(
                        text = "Share Label",
                        icon = Icons.Default.Share,
                        onClick = {
                            QrCodeGenerator.shareProductLabel(context, product)
                        },
                        modifier = Modifier.weight(1.3f)
                    )
                }
            }
        }
    }
}
