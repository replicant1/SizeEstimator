package com.example.sizeestimator

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.annotation.VisibleForTesting
import com.example.sizeestimator.MainActivity.Companion.ANALYSED_IMAGE_DIR
import com.example.sizeestimator.ml.SsdMobilenetV1
import org.tensorflow.lite.support.image.TensorImage
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class AnalysableBitmapFile(private val bitmapFile: File) {

    /**
     * @param context application context
     */
    fun analyse(context: Context): AnalysisResult {
        val bitmapToAnalyse = BitmapFactory.decodeFile(bitmapFile.absolutePath)
        val model = SsdMobilenetV1.newInstance(context)
        val image = TensorImage.fromBitmap(bitmapToAnalyse)
        val outputs = model.process(image)
        val analyser = Analyser(outputs.detectionResultList, 150F)

        model.close()

        return analyser.analyse()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun markup(analysisResult: AnalysisResult) {
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

        val immutableBitmap = BitmapFactory.decodeFile(bitmapFile.absolutePath)
        val mutableBitmap = Bitmap.createBitmap(
            immutableBitmap.width,
            immutableBitmap.height,
            Bitmap.Config.RGB_565
        )
        val canvas = Canvas(mutableBitmap)

        // Copy mutable bitmap to the canvas so that we can draw on top of it
        canvas.drawBitmap(immutableBitmap, 0F, 0F, rectPaint)

        analysisResult.sortedResults.forEachIndexed { index: Int, result ->
            if ((index == analysisResult.targetObjectIndex) || (index == analysisResult.referenceObjectIndex)) {
                rectPaint.pathEffect = null
            } else {
                rectPaint.pathEffect = DashPathEffect(floatArrayOf(1F, 1F), 1F)
            }
            rectPaint.color = RECT_COLORS[index % RECT_COLORS.size]
            canvas.drawRect(result.locationAsRectF, rectPaint)
        }

        // Draw the legend at top left of the image
        analysisResult.sortedResults.forEachIndexed { index: Int, result ->
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

    companion object {

        /**
         * Static factory method. Create the lo-res equivalent of the raw camera image that can
         * be analysed and processed.
         *
         * @param cameraBitmapFile raw image produced by the camera. Assumed to be in landscape orientation.
         * @return AnalysableBitmapFile that represents the given [cameraBitmapFile] transformed into
         * a size and format suitable for TensorFlow processing. Will have dimensions [TENSOR_FLOW_IMAGE_SIZE_PX]
         */
        fun fromCameraBitmapFile(cameraBitmapFile: File): AnalysableBitmapFile? {
            val rawBitmap = BitmapFactory.decodeFile(cameraBitmapFile.absolutePath)
            Log.d(
                TAG,
                "Cropping bitmap of width = ${rawBitmap.width}, height = ${rawBitmap.height}"
            )

            // Cropping this much off width should make image square
            // NOTE: Assuming width > height
            val horizontalCrop = (rawBitmap.width - rawBitmap.height) / 2
            val squaredBitmap = Bitmap.createBitmap(
                rawBitmap,
                horizontalCrop,
                0,
                rawBitmap.height,
                rawBitmap.height
            )

            Log.d(
                TAG,
                "Squared bitmap has height = ${squaredBitmap.height}, width = ${squaredBitmap.width}"
            )

            // Scale down to size expected by TensorFlow model
            val scaledSquareBitmap = Bitmap.createScaledBitmap(
                squaredBitmap, TENSOR_FLOW_IMAGE_SIZE_PX, TENSOR_FLOW_IMAGE_SIZE_PX, false
            )
            Log.d(
                TAG,
                "Scaled square bitmap has width = ${scaledSquareBitmap.width}, height = ${scaledSquareBitmap.height}"
            )

            // Save the cropped and scaled bitmap
            val file =  saveBitmapToUniqueFilename(scaledSquareBitmap)
            return if (file != null) {
                AnalysableBitmapFile(file)
            } else {
                null
            }
        }

        /**
         * @return The bitmaps filename, or null if couldn't save
         */
        @RequiresApi(Build.VERSION_CODES.O)
        @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
        fun saveBitmapToUniqueFilename(bitmapImage: Bitmap): File? {
            var result: File? = null

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

                Log.d(TAG, "Wrote bitmap OK to file $path")

                result = File(path)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }

            return result
        }

        private val TAG = AnalysableBitmapFile::class.java.simpleName
        private const val TENSOR_FLOW_IMAGE_SIZE_PX = 300
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