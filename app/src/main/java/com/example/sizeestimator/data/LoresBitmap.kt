package com.example.sizeestimator.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Typeface
import com.example.sizeestimator.domain.MeasurementTrace
import com.example.sizeestimator.domain.Scoreboard
import com.example.sizeestimator.domain.toRectF
import com.example.sizeestimator.domain.toScoreboardItemList
import com.example.sizeestimator.ml.SsdMobilenetV1
import org.tensorflow.lite.support.image.TensorImage

/**
 * A small bitmap that has been scaled down and cropped from a raw camera image, and is small enough
 * for the Tensor Flow model to process.
 */
class LoresBitmap(private var squareBitmap: Bitmap) {
    companion object {
        fun fromHiresBitmap(hiresBitmap: Bitmap): LoresBitmap {
            return LoresBitmap(hiresBitmap.toSquare(LORES_IMAGE_SIZE_PX))
        }
        const val LORES_IMAGE_SIZE_PX = 300
    }

    fun saveToAppCache(context : Context, filename: String) {
        squareBitmap.saveToAppCache(context, filename)
    }

    fun score(context : Context) : Scoreboard {
        val model = SsdMobilenetV1.newInstance(context)
        val image = TensorImage.fromBitmap(squareBitmap)
        val outputs = model.process(image)
        model.close()
        return Scoreboard(outputs.detectionResultList.toScoreboardItemList())
    }

    /**
     * Draw bounding boxes, scores and a legend on this [LoresBitmap] that visualizes
     * the given [measurementTrace].
     */
    fun markup(measurementTrace: MeasurementTrace) {
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
            squareBitmap.width,
            squareBitmap.height,
            Bitmap.Config.RGB_565
        )
        val canvas = Canvas(mutableBitmap)

        // Copy mutable bitmap to the canvas so that we can draw on top of it
        canvas.drawBitmap(squareBitmap, 0F, 0F, rectPaint)

        measurementTrace.sortedResults.list.forEachIndexed { index, result ->
            // Make the bounding boxes for reference and target objects standout as solid while
            // others are dashed.
            if ((result == measurementTrace.targetObject) || (result == measurementTrace.referenceObject)) {
                rectPaint.pathEffect = null
            } else {
                rectPaint.pathEffect = DashPathEffect(floatArrayOf(1F, 1F), 1F)
            }
            rectPaint.color = MARKUP_COLORS[index % MARKUP_COLORS.size]
            canvas.drawRect(result.location.toRectF(), rectPaint)
        }

        // Draw the legend at top left of the image
        measurementTrace.sortedResults.list.forEachIndexed { index, result ->
            legendPaint.color = MARKUP_COLORS[index]
            canvas.drawRect(
                android.graphics.Rect(
                    10,
                    10 + (index * 20),
                    20,
                    20 + (index * 20)
                ),
                legendPaint
            )

            textPaint.color = MARKUP_COLORS[index]
            canvas.drawText(
                result.score.toString(),
                25F,
                20F + (index * 20),
                textPaint
            )
        }
        squareBitmap = mutableBitmap
    }

    val MARKUP_COLORS: List<Int> =
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