package com.tindatrack.qr

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Shared multiplatform QR scanner view.
 * Android: CameraX + Google ML Kit Barcode Scanning
 * iOS: AVFoundation (AVCaptureSession + AVCaptureMetadataOutput)
 */
@Composable
expect fun QrScannerView(
    onCodeScanned: (String) -> Unit,
    modifier: Modifier = Modifier
)
