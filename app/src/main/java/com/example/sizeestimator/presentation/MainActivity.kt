package com.example.sizeestimator.presentation

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.sizeestimator.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


/**
 * This code is taken substantially from the Google Codelab on Camera X:
 * https://developer.android.com/codelabs/camerax-getting-started#0
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private lateinit var viewBinding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel

    private val activityResultLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        )
        { permissions ->
            var permissionGranted = true
            permissions.entries.forEach {
                if (it.key in REQUIRED_PERMISSIONS && !it.value)
                    permissionGranted = false
            }
            if (!permissionGranted) {
                viewModel.onError("Permission request denied")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        setContentView(viewBinding.root)

        viewBinding.composeView.setContent {
            val context = LocalContext.current
            val lifecycleOwner = LocalLifecycleOwner.current
            val imageCapture = remember {
                ImageCapture.Builder().build()
            }
            val preview = Preview.Builder().build()
            val previewView = remember {
                PreviewView(context)
            }

            val cameraxSelector = CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()

            LaunchedEffect(CameraSelector.LENS_FACING_BACK) {
                val cameraProvider = context.getCameraProvider()
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(lifecycleOwner, cameraxSelector, preview, imageCapture)
                preview.setSurfaceProvider(previewView.surfaceProvider)
            }

            val cameraController = remember {
                LifecycleCameraController(context).apply {
                    bindToLifecycle(lifecycleOwner)
                }
            }
            Row() {
                AndroidView(
                    modifier = Modifier.size(300.dp),
                    factory = { ctx ->
                        previewView.apply {
                            scaleType = PreviewView.ScaleType.FILL_START
                            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                            controller = cameraController
                        }
                    },
                    onRelease = { cameraController.unbind() }
                )
                MeasureButtonPanel(
                    viewModel.sizeText.observeAsState().value,
                    viewModel.progressMonitorVisible.observeAsState().value,
                    {
                        viewModel.progressMonitorVisible.value = true
                        captureImage(imageCapture, context)
                    },
                    viewModel.errorFlow
                )
            }
        }

        if (!allPermissionsGranted()) {
            requestPermissions()
        }
    }

    private fun captureImage(imageCapture: ImageCapture, context: Context) {
        val hiresPath = application.cacheDir.absolutePath + File.separator + HIRES_FILENAME
        val hiresFileOutputOptions = ImageCapture.OutputFileOptions.Builder(
            File(hiresPath)
        ).build()

        imageCapture.takePicture(
            hiresFileOutputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    viewModel.onError("Photo capture failed")
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    viewModel.onHiresImageSaved(hiresPath, applicationContext)
                }
            }
        )
    }

    private suspend fun Context.getCameraProvider(): ProcessCameraProvider =
        suspendCoroutine { continuation ->
            ProcessCameraProvider.getInstance(this).also { cameraProvider ->
                cameraProvider.addListener(
                    { continuation.resume(cameraProvider.get()) },
                    ContextCompat.getMainExecutor(this)
                )
            }
        }

    private fun requestPermissions() {
        activityResultLauncher.launch(REQUIRED_PERMISSIONS)
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        private val REQUIRED_PERMISSIONS = mutableListOf(Manifest.permission.CAMERA).toTypedArray()
        private const val HIRES_FILENAME = "hires.jpg"
    }
}
