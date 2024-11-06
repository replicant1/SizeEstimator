package com.example.sizeestimator.data

import android.graphics.Canvas
import android.graphics.Paint
import com.example.sizeestimator.domain.MeasurementTrace

class BoundingBoxesDrawer : MTDrawer {

    /**
     * Draws the bounding boxes in the [trace] in the appropriate colours and patterns. Boxes
     * for reference and target objects are solid, others are dashed.
     */
    override fun draw(bitmap: LoresBitmap, trace: MeasurementTrace) {
        val canvas = Canvas(bitmap.squareBitmap)

        val boxPaint = Paint().apply {
            style = Paint.Style.STROKE
            strokeWidth = MTDrawer.BOX_STROKE_WIDTH_PX
            isAntiAlias = false
        }

        trace.scoreboard.list.forEachIndexed { index, item ->
            boxPaint.pathEffect =
                if ((item == trace.targetObject) || (item == trace.referenceObject)) {
                    null
                } else {
                    MTDrawer.BOX_STROKE_EFFECT
                }
            boxPaint.color = MTDrawer.MARK_UP_COLORS[index % MTDrawer.MARK_UP_COLORS.size]
            canvas.drawRect(item.location.toRectF(), boxPaint)
        }
    }
}