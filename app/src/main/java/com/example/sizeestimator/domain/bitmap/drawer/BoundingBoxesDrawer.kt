package com.example.sizeestimator.domain.bitmap.drawer

import android.graphics.Canvas
import android.graphics.Paint
import com.example.sizeestimator.domain.MeasurementTrace
import com.example.sizeestimator.domain.bitmap.LoresBitmap
import com.example.sizeestimator.domain.bitmap.drawer.MTDrawerConstants.MARK_UP_COLORS

class BoundingBoxesDrawer(private val boxStyle : BoundingBoxStyle, override var next: MTDrawer?) : MTDrawer {
    enum class BoundingBoxStyle {
        FILL, OUTLINE;
    }

    /**
     * Draws the bounding boxes in the [trace] in the appropriate colours and patterns. Boxes
     * for reference and target objects are solid, others are dashed.
     */
    override fun draw(bitmap: LoresBitmap, trace: MeasurementTrace) {
        val canvas = Canvas(bitmap.squareBitmap)

        val boxPaint = Paint().apply {
            style = when (boxStyle) {
                BoundingBoxStyle.FILL -> Paint.Style.FILL
                BoundingBoxStyle.OUTLINE -> Paint.Style.STROKE
            }
            strokeWidth = MTDrawerConstants.BOX_STROKE_WIDTH_PX
            isAntiAlias = false
        }

        trace.scoreboard.list.forEachIndexed { index, item ->
            boxPaint.color = MARK_UP_COLORS[index % MARK_UP_COLORS.size]
            canvas.drawRect(item.location.toRectF(), boxPaint)
        }

        next?.draw(bitmap, trace)
    }
}

