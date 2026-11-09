package com.example.platform

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.NoPhotography
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.ui.components.TindaPrimaryButton
import com.example.ui.theme.*
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

@Composable
fun CameraScannerView(
    onCodeScanned: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    if (hasCameraPermission) {
        CameraPreviewWithMlKit(
            onCodeScanned = onCodeScanned,
            modifier = modifier
        )
    } else {
        CameraPermissionDeniedCard(
            onRequestPermission = { permissionLauncher.launch(Manifest.permission.CAMERA) },
            modifier = modifier
        )
    }
}

@SuppressLint("UnsafeOptInUsageError")
@Composable
private fun CameraPreviewWithMlKit(
    onCodeScanned: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var isTorchOn by remember { mutableStateOf(false) }
    var cameraControl by remember { mutableStateOf<CameraControl?>(null) }

    // 1.5s Duplicate-scan throttle guard
    var lastScannedCode by remember { mutableStateOf<String?>(null) }
    var lastScanTime by remember { mutableLongStateOf(0L) }

    val barcodeScanner = remember { BarcodeScanning.getClient() }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    fun triggerHapticFeedback() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createOneShot(70, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                @Suppress("DEPRECATION")
                vibrator?.vibrate(70)
            }
        } catch (_: Exception) {}
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()

                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()

                    imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                        val mediaImage = imageProxy.image
                        if (mediaImage != null) {
                            val inputImage = InputImage.fromMediaImage(
                                mediaImage,
                                imageProxy.imageInfo.rotationDegrees
                            )

                            barcodeScanner.process(inputImage)
                                .addOnSuccessListener { barcodes ->
                                    for (barcode in barcodes) {
                                        val rawValue = barcode.rawValue ?: continue
                                        val now = System.currentTimeMillis()
                                        // 1.5s duplicate guard
                                        if (rawValue != lastScannedCode || (now - lastScanTime) > 1500L) {
                                            lastScannedCode = rawValue
                                            lastScanTime = now
                                            triggerHapticFeedback()
                                            onCodeScanned(rawValue)
                                            break
                                        }
                                    }
                                }
                                .addOnCompleteListener {
                                    imageProxy.close()
                                }
                        } else {
                            imageProxy.close()
                        }
                    }

                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                    try {
                        cameraProvider.unbindAll()
                        val camera = cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageAnalysis
                        )
                        cameraControl = camera.cameraControl
                    } catch (_: Exception) {}
                }, ContextCompat.getMainExecutor(ctx))

                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        // Viewfinder Target Frame Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .border(3.dp, SageDeep, RoundedCornerShape(24.dp))
                    .background(Color.Black.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
            )
        }

        // Torch / Flash toggle
        IconButton(
            onClick = {
                val targetState = !isTorchOn
                isTorchOn = targetState
                cameraControl?.enableTorch(targetState)
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(20.dp)
                .size(48.dp)
                .clip(CircleShape)
                .background(CardCream.copy(alpha = 0.85f))
        ) {
            Icon(
                imageVector = if (isTorchOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                contentDescription = "Torch",
                tint = if (isTorchOn) AccentGold else Ink
            )
        }
    }
}

@Composable
private fun CameraPermissionDeniedCard(
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground.copy(alpha = 0.9f))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = CardCream),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(SageSoft),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.NoPhotography,
                        contentDescription = null,
                        tint = Forest,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Text(
                    text = "Camera Permission Required",
                    style = TindaTypography.titleMedium,
                    color = Ink
                )

                Text(
                    text = "Tinda Track needs camera access to scan product QR codes and manufacturer barcodes in real time. You can still enter codes manually below.",
                    style = TindaTypography.bodySmall,
                    color = Muted,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                TindaPrimaryButton(
                    text = "Allow Camera Access",
                    onClick = onRequestPermission
                )
            }
        }
    }
}
