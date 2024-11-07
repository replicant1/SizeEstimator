package com.example.sizeestimator.domain.bitmap.drawer

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import com.example.sizeestimator.domain.MeasurementTrace
import com.example.sizeestimator.domain.bitmap.LoresBitmap

class LegendDrawer(override var next: MTDrawer?) : MTDrawer {

    companion object {
         const val LEGEND_MARGIN_PX = 10
         const val LEGEND_BOX_WIDTH_PX = 10
         const val LEGEND_ROW_HEIGHT_PX = 20
         const val LEGEND_BOX_TEXT_GAP_PX = 5
         const val LEGEND_TEXT_SIZE_PX = 14F
    }

    /**
     * Draws a legend at top left into the given [canvas] showing color and score
     * for all bounding boxes in the bitmap.
     */
    override fun draw(lores: LoresBitmap, trace: MeasurementTrace) {
        val canvas = Canvas(lores.squareBitmap)

        val legendPaint = Paint().apply {
            style = Paint.Style.FILL
            strokeWidth = 1f
        }

        val textPaint = Paint().apply {
            textSize = LEGEND_TEXT_SIZE_PX
            typeface = Typeface.MONOSPACE
            strokeWidth = MTDrawer.BOX_STROKE_WIDTH_PX
        }

        trace.scoreboard.list.forEachIndexed { index, item ->
            legendPaint.color = MTDrawer.MARK_UP_COLORS[index]

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
            textPaint.color = MTDrawer.MARK_UP_COLORS[index]
            canvas.drawText(
                item.score.toString(),
                (LEGEND_MARGIN_PX + LEGEND_BOX_WIDTH_PX + LEGEND_BOX_TEXT_GAP_PX).toFloat(),
                (LEGEND_ROW_HEIGHT_PX + (index * LEGEND_ROW_HEIGHT_PX)).toFloat(),
                textPaint
            )
        }
    }
}