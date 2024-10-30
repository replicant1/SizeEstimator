package com.example.sizeestimator.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import com.example.sizeestimator.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


/**
 * This code is taken substantially from the Google Codelab on Camera X:
 * https://developer.android.com/codelabs/camerax-getting-started#0
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private lateinit var viewBinding: ActivityMainBinding
    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var viewModel: MainViewModel

    private val activityResultLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        )
        { permissions ->
            // Handle Permission granted/rejected
            var permissionGranted = true
            permissions.entries.forEach {
                if (it.key in REQUIRED_PERMISSIONS && !it.value)
                    permissionGranted = false
            }
            if (!permissionGranted) {
                viewModel.onError("Permission request denied")
            } else {
//                startCamera()
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

            val cameraController = remember {
                LifecycleCameraController(context).apply {
                    bindToLifecycle(lifecycleOwner)
                }
            }
            Row() {
                AndroidView(
                    modifier = Modifier.size(300.dp),
                    factory = { ctx ->
                        PreviewView(ctx).apply {
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
                    { /*onMeasureButtonClicked()*/ },
                    viewModel.errorFlow
                )
            }
        }

        // Request camera permissions
        if (allPermissionsGranted()) {
//            startCamera()
        } else {
            requestPermissions()
        }

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

//    private fun onMeasureButtonClicked() {
        // Get a stable reference of the modifiable image capture use case
//        val imageCapture = imageCapture ?: return
//
//        try {
//            // Progress monitor shown here and hidden in viewmodel after processing of captured
//            // image is done.
//            viewModel.progressMonitorVisible.value = true
//
//            val hiresPath = application.cacheDir.absolutePath + File.separator + HIRES_FILENAME
//            Timber.d("tempFilePath = $hiresPath")
//
//            val hiresFileOutputOptions = ImageCapture.OutputFileOptions.Builder(
//                File(hiresPath)
//            ).build()
//
//            if (!application.cacheDir.exists()) {
//                applicationContext.cacheDir.mkdir()
//            }
//
//            // Set up image capture listener, which is triggered after photo has
//            // been taken
//            imageCapture.takePicture(
//                hiresFileOutputOptions,
//                ContextCompat.getMainExecutor(this),
//                object : ImageCapture.OnImageSavedCallback {
//                    override fun onError(exc: ImageCaptureException) {
//                        viewModel.onError("Photo capture failed")
//                    }
//
//                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
//                        viewModel.onHiresImageSaved(hiresPath, applicationContext)
//                    }
//                }
//            )
//        } catch (ie: RuntimeException) {
//            Timber.w(ie)
//        }
//    }

//    private fun startCamera() {
//        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
//
//        cameraProviderFuture.addListener({
//            // Used to bind the lifecycle of cameras to the lifecycle owner
//            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
//
//            // Preview
////            val preview = Preview.Builder()
////                .build()
////                .also {
//////                    it.setSurfaceProvider(viewBinding.previewView.surfaceProvider)
////                }
//
//            imageCapture = ImageCapture.Builder().build()
//
//            // Select back camera as a default
//            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
//
//            try {
//                // Unbind use cases before rebinding
//                cameraProvider.unbindAll()
//
//                // Bind use cases to camera
//                cameraProvider.bindToLifecycle(
//                    this, cameraSelector, /*preview,*/ imageCapture,
//                )
//
//            } catch (exc: Exception) {
//                Timber.e("Use case binding failed", exc)
//            }
//
//        }, ContextCompat.getMainExecutor(this))
//    }

    private fun requestPermissions() {
        activityResultLauncher.launch(REQUIRED_PERMISSIONS)
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        private val REQUIRED_PERMISSIONS = mutableListOf(Manifest.permission.CAMERA).toTypedArray()
        private const val HIRES_FILENAME = "hires.jpg"
    }
}
