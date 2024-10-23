package com.example.sizeestimator

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.os.Environment.DIRECTORY_PICTURES
import android.os.Environment.getExternalStoragePublicDirectory
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.core.content.ContextCompat
import androidx.core.graphics.alpha
import com.example.sizeestimator.databinding.ActivityMainBinding
import com.example.sizeestimator.ml.SsdMobilenetV1
import org.tensorflow.lite.support.image.TensorImage
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class MainActivity : ComponentActivity() {
    private lateinit var viewBinding: ActivityMainBinding
    private var imageCapture: ImageCapture? = null
    private var videoCapture: VideoCapture<Recorder>? = null
    private var recording: Recording? = null
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

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        // Reqwuest camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissions()
        }

        viewBinding.imageCaptureButton.setOnClickListener { takePhoto() }
        viewBinding.videoCaptureButton.setOnClickListener { captureVideo() }

        cameraExecutor = Executors.newSingleThreadExecutor()


        val immutablebitmap = getBitmapFromAsset(this, "test_4.jpg")
        println("** bitmap = $immutablebitmap. bitmap width = ${immutablebitmap?.width} and height = ${immutablebitmap?.height}")

        val model = SsdMobilenetV1.newInstance(this)
        val image = TensorImage.fromBitmap(immutablebitmap)
        val outputs = model.process(image)

        val rectPaint = Paint()
        rectPaint.style = Paint.Style.STROKE
        rectPaint.strokeWidth = 2F
        rectPaint.isAntiAlias = false
        //rectPaint.pathEffect = DashPathEffect(floatArrayOf(4F, 1F), 1F)

        val textPaint = Paint()
        textPaint.textSize = 14F
        textPaint.typeface = Typeface.MONOSPACE
        textPaint.strokeWidth = 2F

        val legendPaint = Paint()
        legendPaint.style = Paint.Style.FILL
        legendPaint.strokeWidth = 2F

        val legendFillPaint = Paint()
        legendFillPaint.style = Paint.Style.FILL
        legendFillPaint.color = Color.parseColor("#50FFFFFF")

        println("** ----------------------------------- **")
        println("** number of detection results = ${outputs.detectionResultList.size}")

        if (immutablebitmap != null) {
            val mutableBitmap = Bitmap.createBitmap(
                immutablebitmap.width,
                immutablebitmap.height,
                Bitmap.Config.RGB_565
            )
            val canvas = Canvas(mutableBitmap)
            canvas.drawBitmap(immutablebitmap, 0F, 0F, rectPaint)

            val descendingScores =
                outputs.detectionResultList.sortedByDescending { it.scoreAsFloat }

            descendingScores.forEachIndexed { index: Int, result ->
                // draw rectangle
                println("** result[$index] = $result")
                println("**    category = $")
                println("**    location as rectangle = ${result.locationAsRectF}")
                println("**    score = ${result.scoreAsFloat}")
                rectPaint.color = RECT_COLORS[index]
                canvas.drawRect(result.locationAsRectF, rectPaint)
            }

//            canvas.drawRect(
//                android.graphics.Rect(10, 10, 120, descendingScores.size * 20),
//                    legendFillPaint)


            // Draw the key
            descendingScores.forEachIndexed { index:Int, result ->
                // legend entry
                legendPaint.color = RECT_COLORS[index]
                canvas.drawRect(
                    android.graphics.Rect(
                        10,
                        10 + (index * 20),
                        20,
                        20 + (index * 20)
                    ),
                    legendPaint
                )

                textPaint.color = RECT_COLORS[index]
                canvas.drawText(
                    result.scoreAsFloat.toString(),
                    25F,
                    20F + (index * 20),
                    textPaint
                )
            }

            saveBitmap(mutableBitmap)

        }

        model.close()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun saveBitmap(bitmapImage: Bitmap) {
        if (!ANALYSED_IMAGE_DIR.exists()) {
            ANALYSED_IMAGE_DIR.mkdir()
        }

        val path = ANALYSED_IMAGE_DIR.absolutePath +
                File.separator +
                LocalDateTime.now().format(
                    DateTimeFormatter.ofPattern("dd MMM yyyy hh-mm-ss a A")
                ) +
                ".jpg"

        println("** About to write to file: $path")

        try {
            val fileOutputStream = FileOutputStream(path)
            bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
            fileOutputStream.close()

            println("** Wrote OK to file $path")

        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun getBitmapFromAsset(context: Context, filePath: String): Bitmap? {
        val assetManager = context.assets

        var istr: InputStream? = null
        var bitmap: Bitmap? = null
        try {
            istr = assetManager.open(filePath)
            bitmap = BitmapFactory.decodeStream(istr)
        } catch (iox: IOException) {
            println("** bitmap reading exception: $iox")
        }
        return bitmap
    }

    private fun takePhoto() {

        println("*** Into takePhoto ***")
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return

        println("** imageCapture = $imageCapture")

        // Create time stamped name and MediaStore entry.
        try {
            println("** about to dateformat **")
            val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
                .format(System.currentTimeMillis())
            println("** name = $name")

            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, name)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                    put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image")
                }
            }

            // Create output options object which contains file + metadata
            val outputOptions = ImageCapture.OutputFileOptions
                .Builder(
                    contentResolver,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    contentValues
                )
                .build()

            println("** outputOptions = $outputOptions")

            // Set up image capture listener, which is triggered after photo has
            // been taken
            imageCapture.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(this),
                object : ImageCapture.OnImageSavedCallback {
                    override fun onError(exc: ImageCaptureException) {
                        Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                    }

                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                        val msg = "Photo capture succeeded: ${output.savedUri}"
                        Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                        Log.d(TAG, msg)
                    }
                }
            )
        } catch (ie: RuntimeException) {
            println("** ie = $ie")
        }
    }

    private fun captureVideo() {}

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewBinding.viewFinder.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder().build()

            val imageAnalyzer = ImageAnalysis.Builder()
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, SizeAnalyzer())
                }

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture,// imageAnalyzer
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
        private const val TAG = "CameraXApp"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
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
                    + "My Fit Pro"
        )
        val RECT_COLORS: List<Int> =
            listOf(
                Color.RED,
                Color.YELLOW,
                Color.BLUE,
                Color.CYAN,
                Color.BLACK,
                Color.DKGRAY,
                Color.GRAY,
                Color.GREEN,
                Color.LTGRAY,
                Color.MAGENTA
            )

    }
}
