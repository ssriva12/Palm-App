package com.palmlens.ui.scanner.camera

import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.palmlens.ui.theme.Spacing
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Live CameraX preview + shutter. [onCaptured] receives the raw JPEG bytes (unprocessed) on
 * the main thread; hand them straight to [com.palmlens.data.image.ImagePreprocessor].
 * The capture callback runs on a dedicated background executor, never the main looper.
 */
@Composable
fun CameraCapture(
    modifier: Modifier = Modifier,
    onCaptured: (ByteArray) -> Unit,
    onError: (Throwable) -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    val captureExecutor = remember { Executors.newSingleThreadExecutor() }
    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
    }
    var capturing by remember { mutableStateOf(false) }
    var bindFailed by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose { captureExecutor.shutdown() }
    }

    Box(modifier, contentAlignment = Alignment.BottomCenter) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                val previewView = PreviewView(ctx).apply {
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                }
                val providerFuture = ProcessCameraProvider.getInstance(ctx)
                providerFuture.addListener({
                    try {
                        val provider = providerFuture.get()
                        val preview = Preview.Builder().build().apply {
                            setSurfaceProvider(previewView.surfaceProvider)
                        }
                        provider.unbindAll()
                        provider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            imageCapture,
                        )
                    } catch (t: Throwable) {
                        bindFailed = true
                        onError(t)
                    }
                }, ContextCompat.getMainExecutor(ctx))
                previewView
            },
        )

        PalmGuideOverlay(Modifier.fillMaxSize())

        ShutterButton(
            capturing = capturing,
            enabled = !capturing && !bindFailed,
            modifier = Modifier.padding(bottom = Spacing.space20),
            onClick = {
                capturing = true
                scope.launch {
                    try {
                        onCaptured(imageCapture.takeJpegBytes(captureExecutor))
                    } catch (t: Throwable) {
                        onError(t)
                    } finally {
                        capturing = false
                    }
                }
            },
        )
    }
}

@Composable
private fun ShutterButton(
    capturing: Boolean,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = CircleShape,
        color = Color.White,
        modifier = modifier.size(66.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (capturing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(28.dp),
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun PalmGuideOverlay(modifier: Modifier) {
    val stroke = MaterialTheme.colorScheme.primary
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        drawOval(
            color = stroke.copy(alpha = 0.8f),
            topLeft = Offset(w * 0.16f, h * 0.12f),
            size = Size(w * 0.68f, h * 0.66f),
            style = Stroke(width = 3.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(24f, 20f))),
        )
    }
}

private suspend fun ImageCapture.takeJpegBytes(executor: Executor): ByteArray =
    suspendCancellableCoroutine { cont ->
        takePicture(
            executor,
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    try {
                        val buffer = image.planes[0].buffer
                        val bytes = ByteArray(buffer.remaining())
                        buffer.get(bytes)
                        cont.resume(bytes)
                    } catch (t: Throwable) {
                        cont.resumeWithException(t)
                    } finally {
                        image.close()
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    cont.resumeWithException(exception)
                }
            },
        )
    }
