package com.example.sizeestimator.presentation

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import org.intellij.lang.annotations.JdkConstants.HorizontalAlignment
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Composable
fun SizeEstimatorScreen(viewModel: MainViewModel) {
    val lensFacing = CameraSelector.LENS_FACING_BACK
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current

    val cameraController = remember {
        LifecycleCameraController(context).apply {
            // Bind the LifecycleCameraController to the lifecycleOwner
            bindToLifecycle(lifecycleOwner)
        }
    }

    val preview = Preview.Builder().build()
    val previewView = remember {
        PreviewView(context)
    }
    val cameraxSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
    val imageCapture = remember {
        ImageCapture.Builder().build()
    }
    LaunchedEffect(lensFacing) {
        val cameraProvider = context.getCameraProvider()
        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(lifecycleOwner, cameraxSelector, preview, imageCapture)
        preview.setSurfaceProvider(previewView.surfaceProvider)
    }

    Row (modifier = Modifier.fillMaxSize().background(Color.Yellow)) {
        AndroidView(
            modifier = Modifier.size(300.dp),
            factory = { ctx ->
                previewView.apply {
                    scaleType = PreviewView.ScaleType.FILL_START
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    controller = cameraController
                }
            },
            onRelease = {
                cameraController.unbind()
            }
        )

    }
}

private suspend fun Context.getCameraProvider(): ProcessCameraProvider =
    suspendCoroutine { continuation ->
        ProcessCameraProvider.getInstance(this).also { cameraProvider ->
            cameraProvider.addListener({
                continuation.resume(cameraProvider.get())
            }, ContextCompat.getMainExecutor(this))
        }
    }