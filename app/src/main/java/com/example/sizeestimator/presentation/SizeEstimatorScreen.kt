package com.example.sizeestimator.presentation

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.sizeestimator.R
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Composable
fun SizeEstimatorScreen(viewModel: MainViewModel) {
    val lensFacing = CameraSelector.LENS_FACING_BACK
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current

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
    Row (modifier = Modifier
        .fillMaxSize()) {

        AndroidView(
            modifier = Modifier
                .weight(1F)
                .drawWithContent {
                    drawContent()
                    val centerX = size.width / 2
                    val centerY = size.height / 2
                    val targetSize = 50f
                    val strokeWidth = 4f
                    val targetColor = Color.Blue
                    drawLine(
                        targetColor,
                        strokeWidth = strokeWidth,
                        start = Offset(centerX - targetSize, centerY),
                        end = Offset(centerX + targetSize, centerY)
                    )
                    drawLine(
                        targetColor,
                        strokeWidth = strokeWidth,
                        start = Offset(centerX, centerY - targetSize),
                        end = Offset(centerX, centerY + targetSize)
                    )
                    if (size.width >= size.height) {
                        drawRect(
                            color = Color.Gray,
                            topLeft = Offset(
                                centerX - (size.height / 2),
                                centerY - (size.height / 2)
                            ),
                            size = Size(size.height, size.height),
                            style = Stroke(width = 2f)
                        )
                    }
                },
            factory = { ctx ->
                previewView.apply {
                    scaleType = PreviewView.ScaleType.FILL_END
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                }
            }
        )

        Box(modifier = Modifier.weight(1F)) {
            MeasureButtonPanel(
                sizeText = viewModel.sizeText,
                progressMonitorVisible = viewModel.progressMonitorVisible,
                {
                    val hiresPath = context.cacheDir.absolutePath + File.separator + MainViewModel.HIRES_FILENAME
                    val hiresOutputOptions = ImageCapture.OutputFileOptions.Builder(File(hiresPath))
                        .build()

                    imageCapture.takePicture(hiresOutputOptions,
                        ContextCompat.getMainExecutor(context),
                        object: ImageCapture.OnImageSavedCallback {
                            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                viewModel.onHiresImageSaved(hiresPath, context)

                            }

                            override fun onError(exception: ImageCaptureException) {
                                viewModel.onError(context.getString(R.string.photo_capture_failed))
                            }
                        })
                },
                viewModel.errorFlow)
        }
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