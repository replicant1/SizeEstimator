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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
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
import com.example.sizeestimator.data.LoresBitmap
import com.example.sizeestimator.domain.AnalysisResult
import com.example.sizeestimator.domain.BoundingBox
import timber.log.Timber
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Composable
fun SizeEstimatorScreen(viewModel: MainViewModel, analysisResult: LiveData<AnalysisResult>) {
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

    val analysisResultState = analysisResult.observeAsState()
    Timber.d("analysisResultState.value = ${analysisResultState.value}")

    val referenceBox = remember {
        derivedStateOf {
            val sortedResults = analysisResultState.value?.sortedResults
            val referenceObjectIndex = analysisResultState.value?.referenceObjectIndex
            var boundingBox: BoundingBox? = null
            if ((sortedResults != null) && (referenceObjectIndex != null) && (referenceObjectIndex != -1)) {
                val referenceObject = sortedResults[referenceObjectIndex]
                boundingBox = referenceObject.location
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

        AndroidView(
            modifier = Modifier
                .aspectRatio(4f / 3f) // same as Preview, PreviewView and ImageCapture
                .drawWithCache {
                    val centerX = size.width / 2
                    val centerY = size.height / 2

                    // Cross-hair at center
                    val crossHairColor = Color.Blue
                    val crossHairSize = 50f
                    val crossHairLeft = Offset(centerX - crossHairSize, centerY)
                    val crossHairRight = Offset(centerX + crossHairSize, centerY)
                    val crossHairTop = Offset(centerX, centerY - crossHairSize)
                    val crossHairBottom = Offset(centerX, centerY + crossHairSize)

                    // Bounding boxes for reference and target objects
                    val boxXLeftOffset = (size.width - size.height) / 2
                    val boxStrokeWidth = 4f
                    val referenceBoxColor = Color.Red
                    val targetBoxColor = Color.Green

                    // Lores square viewport in center
                    val loresSquareTopLeft = Offset(
                        centerX - (size.height / 2),
                        centerY - (size.height / 2)
                    )
                    val loresSquareSize = Size(size.height, size.height)
                    val loresSquareStyle = Stroke(width = boxStrokeWidth)

                    // Scaling factor (assume landscape) - from lores to preview
                    val scale = size.height / LoresBitmap.LORES_IMAGE_SIZE_PX.toFloat()

                    val referenceBoxTopLeft = Offset(
                        boxXLeftOffset + referenceBox.value.left * scale,
                        referenceBox.value.top * scale
                    )
                    val referenceBoxSize = Size(
                        referenceBox.value.width() * scale,
                        referenceBox.value.height() * scale
                    )
                    val targetBoxTopLeft = Offset(
                        boxXLeftOffset + targetBox.value.left * scale,
                        targetBox.value.top * scale
                    )
                    val targetBoxSize = Size(
                        targetBox.value.width() * scale,
                        targetBox.value.height() * scale
                    )

                    onDrawWithContent {
                        drawContent()

                        // Horizontal stroke of the cross-hair
                        drawLine(
                            crossHairColor,
                            strokeWidth = boxStrokeWidth,
                            start = crossHairLeft,
                            end = crossHairRight
                        )
                        // Vertical stroke of the cross-hair
                        drawLine(
                            crossHairColor,
                            strokeWidth = boxStrokeWidth,
                            start = crossHairTop,
                            end = crossHairBottom
                        )

                        // Square that anticipates the lores bitmap that
                        // will ultimately be analysed by the Tensor Flow model.
                        // Reference and target objects should be positioned within this.
                        drawRect(
                            color = crossHairColor,
                            topLeft = loresSquareTopLeft,
                            size = loresSquareSize,
                            style = loresSquareStyle
                        )

                        // Bounding box of the reference object as per analysis
                        drawRect(
                            color = referenceBoxColor,
                            topLeft = referenceBoxTopLeft,
                            size = referenceBoxSize,
                            style = Stroke(width = boxStrokeWidth)
                        )

                        // Bounding box of the target object as per analysis
                        drawRect(
                            color = targetBoxColor,
                            topLeft = targetBoxTopLeft,
                            size = targetBoxSize,
                            style = Stroke(width = boxStrokeWidth)
                        )
                    }
                },
            factory = {
                previewView.apply {
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    setBackgroundColor(Color.Yellow.toArgb())
                }
            }
        )

        Box(
            modifier = Modifier
                .weight(1F)
                .size(100.dp)
        ) {
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