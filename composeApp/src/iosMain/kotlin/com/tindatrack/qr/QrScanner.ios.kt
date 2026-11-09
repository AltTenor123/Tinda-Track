package com.tindatrack.qr

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.interop.UIKitView
import androidx.compose.ui.unit.dp
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.*
import platform.Foundation.NSError
import platform.UIKit.UIView
import platform.darwin.NSObject
import platform.darwin.dispatch_get_main_queue

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun QrScannerView(
    onCodeScanned: (String) -> Unit,
    modifier: Modifier
) {
    var lastScannedCode by remember { mutableStateOf<String?>(null) }
    var lastScanTime by remember { mutableLongStateOf(0L) }

    Box(modifier = modifier.fillMaxSize()) {
        UIKitView(
            factory = {
                val containerView = UIView()
                val captureSession = AVCaptureSession()
                val captureDevice = AVCaptureDevice.defaultDeviceWithMediaType(AVMediaTypeVideo)

                if (captureDevice != null) {
                    val input = AVCaptureDeviceInput.deviceInputWithDevice(captureDevice, null)
                    if (input != null && captureSession.canAddInput(input)) {
                        captureSession.addInput(input)
                    }

                    val metadataOutput = AVCaptureMetadataOutput()
                    if (captureSession.canAddOutput(metadataOutput)) {
                        captureSession.addOutput(metadataOutput)

                        val delegate = object : NSObject(), AVCaptureMetadataOutputObjectsDelegateProtocol {
                            override fun captureOutput(
                                output: AVCaptureOutput,
                                didOutputMetadataObjects: List<*>,
                                fromConnection: AVCaptureConnection
                            ) {
                                for (metadata in didOutputMetadataObjects) {
                                    val readable = metadata as? AVMetadataMachineReadableCodeObject ?: continue
                                    val stringValue = readable.stringValue ?: continue
                                    val now = platform.Foundation.NSDate().timeIntervalSince1970.toLong() * 1000L

                                    if (stringValue != lastScannedCode || (now - lastScanTime) > 1500L) {
                                        lastScannedCode = stringValue
                                        lastScanTime = now
                                        onCodeScanned(stringValue)
                                    }
                                }
                            }
                        }

                        metadataOutput.setMetadataObjectsDelegate(delegate, dispatch_get_main_queue())
                        metadataOutput.metadataObjectTypes = listOf(
                            AVMetadataObjectTypeQRCode,
                            AVMetadataObjectTypeEAN13Code,
                            AVMetadataObjectTypeUPCECode
                        )
                    }

                    val previewLayer = AVCaptureVideoPreviewLayer.layerWithSession(captureSession)
                    previewLayer.frame = containerView.bounds
                    containerView.layer.addSublayer(previewLayer)
                    captureSession.startRunning()
                }

                containerView
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}
