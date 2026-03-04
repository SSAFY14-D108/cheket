package com.example.cheketqr.presentation.scanner

import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    isScannerEnabled: Boolean,
    onQrDetected: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val latestOnQrDetected by rememberUpdatedState(onQrDetected)
    val latestEnabledState by rememberUpdatedState(isScannerEnabled)

    var previewView by remember { mutableStateOf<PreviewView?>(null) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            PreviewView(ctx).also { created ->
                previewView = created
            }
        }
    )

    DisposableEffect(previewView, lifecycleOwner) {
        val localPreviewView = previewView
        if (localPreviewView == null) {
            onDispose { }
        } else {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            val mainExecutor = ContextCompat.getMainExecutor(context)

            val analyzer = QrCodeAnalyzer(
                isEnabled = { latestEnabledState },
                onQrDetected = latestOnQrDetected
            )

            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                bindUseCases(
                    cameraProvider = cameraProvider,
                    previewView = localPreviewView,
                    lifecycleOwner = lifecycleOwner,
                    analyzer = analyzer,
                    analysisExecutor = cameraExecutor
                )
            }, mainExecutor)

            onDispose {
                runCatching {
                    val provider = cameraProviderFuture.get()
                    provider.unbindAll()
                }
                analyzer.close()
            }
        }
    }
}

private fun bindUseCases(
    cameraProvider: ProcessCameraProvider,
    previewView: PreviewView,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    analyzer: QrCodeAnalyzer,
    analysisExecutor: ExecutorService
) {
    val preview = Preview.Builder().build().also {
        it.surfaceProvider = previewView.surfaceProvider
    }

    val analysis = ImageAnalysis.Builder()
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        .build()
        .also {
            it.setAnalyzer(analysisExecutor, analyzer)
        }

    cameraProvider.unbindAll()
    cameraProvider.bindToLifecycle(
        lifecycleOwner,
        CameraSelector.DEFAULT_BACK_CAMERA,
        preview,
        analysis
    )
}

private class QrCodeAnalyzer(
    private val isEnabled: () -> Boolean,
    private val onQrDetected: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val processing = AtomicBoolean(false)
    private val scanner = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
    )

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        if (!isEnabled() || !processing.compareAndSet(false, true)) {
            imageProxy.close()
            return
        }

        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            processing.set(false)
            imageProxy.close()
            return
        }

        val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

        scanner.process(inputImage)
            .addOnSuccessListener { barcodes ->
                val value = barcodes.firstOrNull()?.rawValue
                if (!value.isNullOrBlank()) {
                    onQrDetected(value)
                }
            }
            .addOnCompleteListener {
                processing.set(false)
                imageProxy.close()
            }
    }

    fun close() {
        scanner.close()
    }
}
