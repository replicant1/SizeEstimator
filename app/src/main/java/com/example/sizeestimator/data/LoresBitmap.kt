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
import java.io.File

/**
 * A small bitmap that has been scaled down and cropped from a raw camera image, and is small enough
 * for the Tensor Flow model to process.
 * @property a bitmap that has been cropped and scaled to have width and height
 */
class LoresBitmap private constructor(private var squareBitmap: Bitmap) {
    companion object {
        fun fromHiresBitmap(hiresBitmap: Bitmap): LoresBitmap {
            return LoresBitmap(hiresBitmap.toSquare(LORES_IMAGE_SIZE_PX))
        }

        const val LORES_IMAGE_SIZE_PX = 300
        private val BOX_STROKE_EFFECT = DashPathEffect(floatArrayOf(1F, 1F), 1F)
        const val LEGEND_MARGIN_PX = 10
        const val LEGEND_BOX_WIDTH_PX = 10
        const val LEGEND_ROW_HEIGHT_PX = 20
        const val LEGEND_BOX_TEXT_GAP_PX = 5
        const val BOX_STROKE_WIDTH = 2F
        const val LEGEND_TEXT_SIZE = 14F
        private val MARK_UP_COLORS: List<Int> =
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

    fun save(dir: File, filename: String) {
        squareBitmap.save(dir, filename)
    }

    fun score(context: Context): Scoreboard {
        val model = SsdMobilenetV1.newInstance(context)
        val image = TensorImage.fromBitmap(squareBitmap)
        val outputs = model.process(image)
        model.close()
        return Scoreboard(outputs.detectionResultList.toScoreboardItemList())
    }

    /**
     * Draw bounding boxes, scores and a legend on this [LoresBitmap] that visualizes
     * the given [trace]. Mutates the bitmap supplied to the constructor.
     */
    fun drawTrace(trace: MeasurementTrace) {
        val mutableBitmap = Bitmap.createBitmap(
            squareBitmap.width,
            squareBitmap.height,
            Bitmap.Config.RGB_565
        )
        val canvas = Canvas(mutableBitmap)

        // Copy mutable bitmap to the canvas then draw legend and bounding
        // boxes on top of it
        canvas.drawBitmap(squareBitmap, 0F, 0F, Paint())
        drawLegend(canvas, trace)
        drawBoundingBoxes(canvas, trace)

        squareBitmap = mutableBitmap
    }

    /**
     * Draws a legend at top left into the given [canvas] showing color and score
     * for all bounding boxes in the []
     */
    private fun drawLegend(canvas: Canvas, trace: MeasurementTrace) {
        val legendPaint = Paint().apply {
            style = Paint.Style.FILL
            strokeWidth = BOX_STROKE_WIDTH
        }

        val textPaint = Paint().apply {
            textSize = LEGEND_TEXT_SIZE
            typeface = Typeface.MONOSPACE
            strokeWidth = BOX_STROKE_WIDTH
        }

        trace.scoreboard.list.forEachIndexed { index, item ->
            legendPaint.color = MARK_UP_COLORS[index]

            // Draw a filled square in the current item's colour
            canvas.drawRect(
                android.graphics.Rect(
                    LEGEND_MARGIN_PX,
                    LEGEND_MARGIN_PX + (index * LEGEND_ROW_HEIGHT_PX),
                    LEGEND_MARGIN_PX + LEGEND_BOX_WIDTH_PX,
                    LEGEND_ROW_HEIGHT_PX + (index * LEGEND_ROW_HEIGHT_PX)
                ),
                legendPaint
            )

            // Draw the numerical score for current item
            textPaint.color = MARK_UP_COLORS[index]
            canvas.drawText(
                item.score.toString(),
                (LEGEND_MARGIN_PX + LEGEND_BOX_WIDTH_PX + LEGEND_BOX_TEXT_GAP_PX).toFloat(),
                (LEGEND_ROW_HEIGHT_PX + (index * LEGEND_ROW_HEIGHT_PX)).toFloat(),
                textPaint
            )
        }
    }

    /**
     * Draws the bounding boxes in the [trace] in the appropriate colours and patterns. Boxes
     * for reference and target objects are solid, others are dashed.
     */
    private fun drawBoundingBoxes(canvas: Canvas, trace: MeasurementTrace) {
        val rectPaint = Paint().apply {
            style = Paint.Style.STROKE
            strokeWidth = BOX_STROKE_WIDTH
            isAntiAlias = false
        }

        trace.scoreboard.list.forEachIndexed { index, item ->
            rectPaint.pathEffect =
                if ((item == trace.targetObject) || (item == trace.referenceObject)) {
                    null
                } else {
                    BOX_STROKE_EFFECT
                }
            rectPaint.color = MARK_UP_COLORS[index % MARK_UP_COLORS.size]
            canvas.drawRect(item.location.toRectF(), rectPaint)
        }
    }
}