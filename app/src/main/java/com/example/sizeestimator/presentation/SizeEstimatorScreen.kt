package com.example.sizeestimator.presentation

import android.content.Context
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.sizeestimator.R
import com.example.sizeestimator.domain.MeasurementTrace
import timber.log.Timber
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Composable
fun SizeEstimatorScreen(viewModel: MainViewModel, trace: LiveData<MeasurementTrace?>) {
    val lensFacing = CameraSelector.LENS_FACING_BACK
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current

    // Preview, ImageCapture and PreviewView must all be 4:3 for what's on the screen
    // and what is analysed by the Tensor Flow model to all correspond.
    val preview = Preview.Builder().setTargetAspectRatio(AspectRatio.RATIO_4_3).build()
    val previewView = remember {
        PreviewView(context)
    }
    val cameraxSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
    val imageCapture = remember {
        ImageCapture.Builder().setTargetAspectRatio(AspectRatio.RATIO_4_3).build()
    }
    LaunchedEffect(lensFacing) {
        val cameraProvider = context.getCameraProvider()
        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(lifecycleOwner, cameraxSelector, preview, imageCapture)
        preview.setSurfaceProvider(previewView.surfaceProvider)
    }
    Row (modifier = Modifier
        .fillMaxSize()) {

        val traceState = trace.observeAsState()
        Timber.d("traceState.value = ${traceState.value}")

        Row(
            modifier = Modifier
                .fillMaxSize()
        ) {
            AndroidView(
                modifier = Modifier
                    .aspectRatio(4f / 3f) // same as Preview, PreviewView and ImageCapture
                    .drawWithCache(drawOverlay(traceState)),
                factory = {
                    previewView.apply {
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                        implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    }
                }
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                MeasureButtonPanel(
                    sizeText = viewModel.sizeText,
                    progressMonitorVisible = viewModel.progressMonitorVisible,
                    {
                        val hiresPath =
                            context.cacheDir.absolutePath + File.separator + MainViewModel.HIRES_FILENAME
                        val hiresOutputOptions =
                            ImageCapture.OutputFileOptions.Builder(File(hiresPath))
                                .build()

                        imageCapture.takePicture(hiresOutputOptions,
                            ContextCompat.getMainExecutor(context),
                            object : ImageCapture.OnImageSavedCallback {
                                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                    viewModel.onHiresImageSaved(hiresPath, context)

                                }

                                override fun onError(exception: ImageCaptureException) {
                                    viewModel.onError(context.getString(R.string.photo_capture_failed))
                                }
                            })
                    },
                    viewModel.errorFlow
                )
            }
        }
    }
}

suspend fun Context.getCameraProvider(): ProcessCameraProvider =
    suspendCoroutine { continuation ->
        ProcessCameraProvider.getInstance(this).also { cameraProvider ->
            cameraProvider.addListener({
                continuation.resume(cameraProvider.get())
            }, ContextCompat.getMainExecutor(this))
        }
    }