package com.example.sizeestimator.presentation

import android.content.Context
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.sizeestimator.domain.AnalysisResult
import com.example.sizeestimator.domain.BoundingBox
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Composable
fun SizeEstimatorScreen(viewModel: MainViewModel, analysisResult: LiveData<AnalysisResult>) {
    val lensFacing = CameraSelector.LENS_FACING_BACK
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current

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

    val analysisResultState = analysisResult.observeAsState()
    println("**** analysisResultState.value = ${analysisResultState.value}")


    val referenceBox = remember {
        derivedStateOf {
            val sortedResults = analysisResultState.value?.sortedResults
            println("**** sortedresults = $sortedResults")
            val referenceObjectIndex = analysisResultState.value?.referenceObjectIndex
            println("*** referenceObjectIndex = $referenceObjectIndex")
            var boundingBox: BoundingBox? = null
            if ((sortedResults != null) && (referenceObjectIndex != null) && (referenceObjectIndex != -1)) {
                val referenceObject = sortedResults[referenceObjectIndex]
                println("*** referenceObject = $referenceObject")
                boundingBox = referenceObject.location
                println("*** boundingBox = $boundingBox")
            }
            return@derivedStateOf boundingBox ?: BoundingBox(10f, 10f, 20f, 20f)
        }
    }

    val targetBox = remember {
        derivedStateOf {
            val sortedResults = analysisResultState.value?.sortedResults
            val targetObjectIndex = analysisResult.value?.targetObjectIndex
            var boundingBox: BoundingBox? = null
            if ((sortedResults != null) && (targetObjectIndex != null) && (targetObjectIndex != -1)) {
                val targetObject = sortedResults[targetObjectIndex]
                boundingBox = targetObject.location
            }
            return@derivedStateOf boundingBox ?: BoundingBox(10f, 10f, 20f, 20f)
        }
    }


    Row(
        modifier = Modifier
            .fillMaxSize()
    ) {

        if (analysisResultState.value != null) {
            println("** new analysisresultState.value ")
        }

        if (referenceBox.value != null) {
            println("** new binky = ${referenceBox.value}")
        }

        AndroidView(
            modifier = Modifier
                .drawWithContent {
                    drawContent()
                    val centerX = size.width / 2
                    val centerY = size.height / 2
                    val targetSize = 50f
                    val strokeWidth = 4f
                    val targetColor = Color.Blue
                    // Horizontal stroke of the cross-hair
                    drawLine(
                        targetColor,
                        strokeWidth = strokeWidth,
                        start = Offset(centerX - targetSize, centerY),
                        end = Offset(centerX + targetSize, centerY)
                    )
                    // Vertical stroke of the cross-hair
                    drawLine(
                        targetColor,
                        strokeWidth = strokeWidth,
                        start = Offset(centerX, centerY - targetSize),
                        end = Offset(centerX, centerY + targetSize)
                    )
                    drawLine(
                        targetColor,
                        strokeWidth = strokeWidth,
                        start = Offset(0f, 0f),
                        end = Offset(size.width, size.height)
                    )
                    drawLine(
                        targetColor,
                        strokeWidth = strokeWidth,
                        start = Offset(size.width, 0f),
                        end = Offset(0f, size.height)
                    )
//                    println("** size.width = ${size.width}, size.height=${size.height}")
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

                    val loresToPreview = size.height / 300f
//                    val boxXOffset = 0//size.width - size.height / 2

                    val boxXOffset = ( size.width - size.height) / 2


                    drawRect(
                        color = Color.Red,
                        topLeft = Offset(
                            boxXOffset + referenceBox.value.left * loresToPreview,
                             referenceBox.value.top * loresToPreview
                        ),
                        size = Size(
                             referenceBox.value.width() * loresToPreview,
                              referenceBox.value.height() * loresToPreview
                        ),
                        style = Stroke(width = 4f)
                    )

                    drawRect(
                        color = Color.Blue,
                        topLeft = Offset(
                            boxXOffset + targetBox.value.left * loresToPreview,
                             targetBox.value.top * loresToPreview
                        ),
                        size = Size(
                             targetBox.value.width() * loresToPreview,
                             targetBox.value.height() * loresToPreview
                        ),
                        style = Stroke(width = 4f)
                    )
//                    println("previewView width x height = ${previewView.width} x ${previewView.height}")
//                    println("previewView.viewPort.aspectRatio = ${previewView.viewPort?.aspectRatio}")
                },
            factory = { ctx ->
                previewView.apply {
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    setBackgroundColor(Color.Yellow.toArgb())
                }
            }
        )

        Box(modifier = Modifier.weight(1F).size(100.dp)) {
            MeasureButtonPanel(
                sizeText = viewModel.sizeText,
                progressMonitorVisible = viewModel.progressMonitorVisible,
                {
                    val hiresPath =
                        context.cacheDir.absolutePath + File.separator + MainViewModel.HIRES_FILENAME
                    val hiresOutputOptions = ImageCapture.OutputFileOptions.Builder(File(hiresPath))
                        .build()

                    imageCapture.takePicture(hiresOutputOptions,
                        ContextCompat.getMainExecutor(context),
                        object : ImageCapture.OnImageSavedCallback {
                            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                viewModel.onHiresImageSaved(hiresPath, context)

                            }

                            override fun onError(exception: ImageCaptureException) {
                                viewModel.onError("Photo capture failed")
                            }
                        })
                },
                viewModel.errorFlow
            )
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