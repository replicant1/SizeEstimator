package com.example.sizeestimator

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.os.Environment.DIRECTORY_PICTURES
import android.os.Environment.getExternalStoragePublicDirectory
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.core.content.ContextCompat
import com.example.sizeestimator.LoresBitmap.AnalysisOptions
import com.example.sizeestimator.databinding.ActivityMainBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class MainActivity : ComponentActivity() {
    private lateinit var viewBinding: ActivityMainBinding
    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService

    private val activityResultLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        )
        { permissions ->
            // Handle Permission granted/rejected
            var permissionGranted = true
            permissions.entries.forEach {
                if (it.key in REQUIRED_PERMISSIONS && it.value == false)
                    permissionGranted = false
            }
            if (!permissionGranted) {
                Toast.makeText(
                    baseContext,
                    "Permission request denied",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                startCamera()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissions()
        }

        viewBinding.imageCaptureButton.setOnClickListener { takePhoto() }
        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    private fun takePhoto() {
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return

        // Create time stamped name and MediaStore entry.
        try {
            val tempFilePath = ANALYSED_IMAGE_DIR.absolutePath + File.separator + "hires.jpg"
            val tempFileOutputOptions = ImageCapture.OutputFileOptions.Builder(
                File(tempFilePath)
            ).build()

            if (!ANALYSED_IMAGE_DIR.exists()) {
                ANALYSED_IMAGE_DIR.mkdir()
            }

            // Set up image capture listener, which is triggered after photo has
            // been taken
            imageCapture.takePicture(
                tempFileOutputOptions,
                ContextCompat.getMainExecutor(this),
                object : ImageCapture.OnImageSavedCallback {
                    override fun onError(exc: ImageCaptureException) {
                        Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                    }

                    @RequiresApi(Build.VERSION_CODES.O)
                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                        Log.d(TAG, "Saved photo to ${output.savedUri}")

                        Log.d(TAG, "About to crop photo to size expected by tensor flow model")
                        val cameraImage = BitmapFactory.decodeFile(tempFilePath)
                        Log.d(
                            TAG,
                            "Camera image: width=${cameraImage.width}, height=${cameraImage.height}"
                        )

                        val loresBitmap = LoresBitmap.fromHiresBitmap(cameraImage)

                        if (loresBitmap != null) {
                            Log.d(TAG, "Analysing the lores image")
                            val result = loresBitmap.analyse(
                                this@MainActivity,
                                AnalysisOptions(LoresBitmap.LORES_IMAGE_SIZE_PX / 2F)
                            )

                            Log.d(TAG, "About to mark up lores image")
                            loresBitmap.markup(result)

                            // Save bitmap
                            loresBitmap.save(ANALYSED_IMAGE_DIR, "lores.jpg")

                            // Put result on screen
                            viewBinding.textView.text =
                                "Size: ${result.targetObjectSizeMillimetres.first} x ${result.targetObjectSizeMillimetres.second} mm"
                        } else {
                            Log.d(TAG, "Failed to crop photo to size expected by tensor flow model")
                        }
                    }
                }
            )
        } catch (ie: RuntimeException) {
            Log.w(TAG, ie)
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewBinding.previewView.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder().build()

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture,
                )

            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

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
        private val TAG = MainActivity::class.java.simpleName
        private val REQUIRED_PERMISSIONS =
            mutableListOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()

        val ANALYSED_IMAGE_DIR = File(
            getExternalStoragePublicDirectory(DIRECTORY_PICTURES).absolutePath
                    + File.separator
                    + "SizeEstimator"
        )
    }
}
