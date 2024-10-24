package com.example.sizeestimator

import android.Manifest
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

        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissions()
        }

        viewBinding.imageCaptureButton.setOnClickListener { takePhoto() }
        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    /**
     * @param imageFile Image ready to analyse with TensorFlow model. Assumed dimensions
     * are TENSOR_FLOW_IMAGE_WIDTH_PX x TENSOR_FLOW_IMAGE_HEIGHT_PX.
     */
    private fun analyseImageFile(imageFile: File): AnalysisResult {
        val bitmapToAnalyse = BitmapFactory.decodeFile(imageFile.absolutePath)

        val model = SsdMobilenetV1.newInstance(this)
        val image = TensorImage.fromBitmap(bitmapToAnalyse)
        val outputs = model.process(image)
        val resultsByScore = outputs.detectionResultList.sortedByDescending { it.scoreAsFloat }

        val referenceObjectIndex = findReferenceObject(resultsByScore)
        val targetObjectIndex = findTargetObject(resultsByScore, referenceObjectIndex)

        val targetObjectSize = if ((referenceObjectIndex != -1) && (targetObjectIndex != -1)) {
            calculateTargetObjectSize(resultsByScore, referenceObjectIndex, targetObjectIndex)
        } else {
            Pair(-1L, -1L)
        }

        model.close()

        return AnalysisResult(outputs, referenceObjectIndex, targetObjectIndex, targetObjectSize)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun markupImageFile(imageFile: File, analysisResult: AnalysisResult) {
        val rectPaint = Paint()
        rectPaint.style = Paint.Style.STROKE
        rectPaint.strokeWidth = 2F
        rectPaint.isAntiAlias = false

        val textPaint = Paint()
        textPaint.textSize = 14F
        textPaint.typeface = Typeface.MONOSPACE
        textPaint.strokeWidth = 2F

        val legendPaint = Paint()
        legendPaint.style = Paint.Style.FILL
        legendPaint.strokeWidth = 2F

        val immutableBitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
        val mutableBitmap = Bitmap.createBitmap(
            immutableBitmap.width,
            immutableBitmap.height,
            Bitmap.Config.RGB_565
        )
        val canvas = Canvas(mutableBitmap)
        // Copy mutable bitmap to the canvas so that we can draw on top of it
        canvas.drawBitmap(immutableBitmap, 0F, 0F, rectPaint)

        val descendingScores =
            analysisResult.outputs.detectionResultList.sortedByDescending { it.scoreAsFloat }

        if (analysisResult.referenceObjectIndex != -1) {
            val referenceObject = descendingScores[analysisResult.referenceObjectIndex]
            rectPaint.color = Color.RED
            canvas.drawRect(referenceObject.locationAsRectF, rectPaint)
        }

        if (analysisResult.targetObjectIndex != -1) {
            val targetObject = descendingScores[analysisResult.targetObjectIndex]
            rectPaint.color = Color.BLUE
            canvas.drawRect(targetObject.locationAsRectF, rectPaint)
        }

        if ((analysisResult.referenceObjectIndex != -1) && (analysisResult.targetObjectIndex != -1)) {
            calculateTargetObjectSize(
                descendingScores,
                analysisResult.referenceObjectIndex,
                analysisResult.targetObjectIndex
            )
        }

        descendingScores.forEachIndexed { index: Int, result ->
            if ((index == analysisResult.targetObjectIndex) || (index == analysisResult.referenceObjectIndex)) {
                rectPaint.pathEffect = null
            } else {
                rectPaint.pathEffect = DashPathEffect(floatArrayOf(1F, 1F), 1F)
            }
            rectPaint.color = RECT_COLORS[index]
            canvas.drawRect(result.locationAsRectF, rectPaint)
        }

        // Draw the key
        descendingScores.forEachIndexed { index: Int, result ->
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
        saveBitmapToUniqueFilename(mutableBitmap)
    }

    /**
     * @return Size of target object as (width, height) in millimetres
     */
    private fun calculateTargetObjectSize(
        sortedResults: List<SsdMobilenetV1.DetectionResult>,
        referenceObjectIndex: Int,
        targetObjectIndex: Int
    ): Pair<Long, Long> {
        val referenceObjectResult = sortedResults[referenceObjectIndex]
        val targetObjectResult = sortedResults[targetObjectIndex]

        val referenceObjectWidthPx = referenceObjectResult.locationAsRectF.width()
        val mmPerPixel = REFERENCE_OBJECT_WIDTH_MM / referenceObjectWidthPx

        val actualTargetObjectWidthMm = targetObjectResult.locationAsRectF.width() * mmPerPixel
        val actualTargetObjectHeightMm = targetObjectResult.locationAsRectF.height() * mmPerPixel

        Log.d(TAG, "Pixel width of reference object = $referenceObjectWidthPx")
        Log.d(TAG, "Scale factor mmPerPixel = $mmPerPixel")
        Log.d(TAG, "Actual target object size (mm): width = $actualTargetObjectWidthMm, height = $actualTargetObjectHeightMm")

        return Pair(actualTargetObjectWidthMm.toLong(), actualTargetObjectHeightMm.toLong())
    }

    /**
     * @param sortedResults Sorted from highest score to lowest score
     * @return Index into [sortedResults] of the reference object. -1 if not found.
     */
    private fun findReferenceObject(sortedResults: List<SsdMobilenetV1.DetectionResult>): Int {
        // Highest scoring result that is below the vertical midpoint = reference object
        sortedResults.forEachIndexed { index: Int, result ->
            if (result.locationAsRectF.top > 150F) {
                return index
            }
        }
        return -1
    }

    /**
     * @param sortedResults Sorted from highest score to lowest score
     * @return Index into [sortedResults] of the target object. -1 if not found.
     */
    private fun findTargetObject(
        sortedResults: List<SsdMobilenetV1.DetectionResult>,
        referenceObjectIndex: Int
    ): Int {
        val referenceObject = sortedResults[referenceObjectIndex]
        // Highest scoring result that is above the reference object = target object
        sortedResults.forEachIndexed { index: Int, result ->
            if (result.locationAsRectF.bottom < referenceObject.locationAsRectF.top) {
                return index
            }
        }
        return -1
    }

    /**
     * @return The saved bitmap's filename, or null if couldn't save
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun saveBitmapToUniqueFilename(bitmapImage: Bitmap) : File? {
        var result : File? = null

        if (!ANALYSED_IMAGE_DIR.exists()) {
            ANALYSED_IMAGE_DIR.mkdir()
        }

        val path = ANALYSED_IMAGE_DIR.absolutePath +
                File.separator +
                LocalDateTime.now().format(
                    DateTimeFormatter.ofPattern("dd MMM yyyy hh-mm-ss a A")
                ) +
                ".jpg"

        Log.d(TAG, "About to save bitmap to file: $path")

        try {
            val fileOutputStream = FileOutputStream(path)
            bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
            fileOutputStream.close()

            Log.d(TAG,"Wrote bitmap OK to file $path")

            result = File(path)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }

        return result
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
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return

        // Create time stamped name and MediaStore entry.
        try {
            val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
                .format(System.currentTimeMillis())
            println("** name = $name")

            val tempFilePath = ANALYSED_IMAGE_DIR.absolutePath + File.separator + "temp.jpg"
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
                        val tempFile = File(tempFilePath)

                        Log.d(TAG, "Saved photo to ${output.savedUri}")

                        Log.d(TAG, "About to crop photo to size expected by tensor flow model")
                        val analysableBitmapFile = cropAndScaleBitmapToAnalysableSize(tempFile)

                        if (analysableBitmapFile != null) {
                            Log.d(TAG, "About to analyse the cropped and scaled image")
                            val results = analyseImageFile(analysableBitmapFile)

                            Log.d(TAG, "About to mark up cropped image")
                            markupImageFile(analysableBitmapFile, results)

                            // Put result on screen
                            viewBinding.textView.text = "Size: ${results.targetObjectSizeMillimetres.first} x ${results.targetObjectSizeMillimetres.second} mm"
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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun cropAndScaleBitmapToAnalysableSize(bitmapFile: File) : File? {
        val rawBitmap = BitmapFactory.decodeFile(bitmapFile.absolutePath)
        Log.d(TAG, "Cropping bitmap of width = ${rawBitmap.width}, height = ${rawBitmap.height}")

        // Cropping this much off width would make image square
        // NOTE: Assuming width > height
        val cropEachSide = (rawBitmap.width - rawBitmap.height) / 2
        val squaredBitmap = Bitmap.createBitmap(
            rawBitmap,
            cropEachSide,
            0,
            rawBitmap.height,
            rawBitmap.height
        )

        Log.d(TAG, "Squared bitmap has height = ${squaredBitmap.height}, width = ${squaredBitmap.width}")

        // Scale down to size expected by TensorFlow model
        val scaledSquareBitmap = Bitmap.createScaledBitmap(
            squaredBitmap, TENSOR_FLOW_IMAGE_WIDTH_PX, TENSOR_FLOW_IMAGE_HEIGHT_PX, false)
        Log.d(TAG, "Scaled square bitmap has width = ${scaledSquareBitmap.width}, height = ${scaledSquareBitmap.height}")

        // Save the cropped and scaled bitmap
        return saveBitmapToUniqueFilename(scaledSquareBitmap)
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
                    this, cameraSelector, preview, imageCapture, // imageAnalyzer
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
        private const val REFERENCE_OBJECT_WIDTH_MM = 123F
        private const val TENSOR_FLOW_IMAGE_WIDTH_PX = 300
        private const val TENSOR_FLOW_IMAGE_HEIGHT_PX = 300
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
