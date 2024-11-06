package com.example.sizeestimator.data

import android.graphics.Color
import android.graphics.DashPathEffect
import com.example.sizeestimator.domain.MeasurementTrace

/**
 * Something that knows how to draw some aspect of a [MeasurementTrace] data set into a [LoresBitmap]
 */
interface MTDrawer {
    /**
     * @param bitmap Mutable bitmap to draw on
     * @param data to be drawn into the bitmap
     */
    fun draw(bitmap: LoresBitmap, trace: MeasurementTrace)

    companion object {
        val BOX_STROKE_EFFECT = DashPathEffect(floatArrayOf(1F, 1F), 1F)
        const val BOX_STROKE_WIDTH_PX = 2F
        val MARK_UP_COLORS: List<Int> =
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