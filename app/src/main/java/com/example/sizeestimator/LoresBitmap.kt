package com.example.sizeestimator

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.annotation.VisibleForTesting
import com.example.sizeestimator.ml.SsdMobilenetV1
import org.tensorflow.lite.support.image.TensorImage
import java.io.File
import java.io.FileOutputStream

class LoresBitmap(private var loresBitmap : Bitmap) {

    data class AnalysisOptions(val minTop: Float)

    fun analyse(context: Context, options : AnalysisOptions): AnalysisResult {
        val model = SsdMobilenetV1.newInstance(context)
        val image = TensorImage.fromBitmap(loresBitmap)
        val outputs = model.process(image)
        val analyser = Analyser(outputs.detectionResultList)

        model.close()

        return analyser.analyse(options)
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

        val mutableBitmap = Bitmap.createBitmap(
            loresBitmap.width,
            loresBitmap.height,
            Bitmap.Config.RGB_565
        )
        val canvas = Canvas(mutableBitmap)

        // Copy mutable bitmap to the canvas so that we can draw on top of it
        canvas.drawBitmap(loresBitmap, 0F, 0F, rectPaint)

        analysisResult.sortedResults.forEachIndexed { index: Int, result ->
            if ((index == analysisResult.targetObjectIndex) || (index == analysisResult.referenceObjectIndex)) {
                rectPaint.pathEffect = null
            } else {
                rectPaint.pathEffect = DashPathEffect(floatArrayOf(1F, 1F), 1F)
            }
            rectPaint.color = LEGEND_COLORS[index % LEGEND_COLORS.size]
            canvas.drawRect(result.locationAsRectF, rectPaint)
        }

        // Draw the legend at top left of the image
        analysisResult.sortedResults.forEachIndexed { index: Int, result ->
            legendPaint.color = LEGEND_COLORS[index]
            canvas.drawRect(
                android.graphics.Rect(
                    10,
                    10 + (index * 20),
                    20,
                    20 + (index * 20)
                ),
                legendPaint
            )

            textPaint.color = LEGEND_COLORS[index]
            canvas.drawText(
                result.scoreAsFloat.toString(),
                25F,
                20F + (index * 20),
                textPaint
            )
        }
        loresBitmap = mutableBitmap
    }

    /**
     * @return The bitmaps filename, or null if couldn't save
     */
    @RequiresApi(Build.VERSION_CODES.O)
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun save(dir : File, filename: String) {
        if (!dir.exists()) {
            dir.mkdir()
        }

        val path = dir.absolutePath + File.separator + filename

        Log.d(TAG, "About to save bitmap to file: $path")

        try {
            val fileOutputStream = FileOutputStream(path)
            loresBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
            fileOutputStream.close()

            Log.d(TAG, "Wrote bitmap OK to file $path")

        } catch (e: java.lang.Exception) {
            Log.w(TAG, e)
        }
    }

    companion object {
        fun fromHiresBitmap(hiresBitmap: Bitmap): LoresBitmap? {
            Log.d(
                TAG,
                "Cropping bitmap of width = ${hiresBitmap.width}, height = ${hiresBitmap.height}"
            )

            // Cropping this much off width should make image square
            // NOTE: Assuming width > height
            val horizontalCrop = (hiresBitmap.width - hiresBitmap.height) / 2
            val squaredBitmap = Bitmap.createBitmap(
                hiresBitmap,
                horizontalCrop,
                0,
                hiresBitmap.height,
                hiresBitmap.height
            )

            Log.d(
                TAG,
                "Squared bitmap has width = ${squaredBitmap.width}, height = ${squaredBitmap.height}"
            )

            // Scale down to size expected by TensorFlow model
            val scaledSquareBitmap = Bitmap.createScaledBitmap(
                squaredBitmap, LORES_IMAGE_SIZE_PX, LORES_IMAGE_SIZE_PX, false
            )
            Log.d(
                TAG,
                "Scaled square bitmap has width = ${scaledSquareBitmap.width}, height = ${scaledSquareBitmap.height}"
            )

            return LoresBitmap(scaledSquareBitmap)
        }

        private val TAG = LoresBitmap::class.java.simpleName
         const val LORES_IMAGE_SIZE_PX = 300
        val LEGEND_COLORS: List<Int> =
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