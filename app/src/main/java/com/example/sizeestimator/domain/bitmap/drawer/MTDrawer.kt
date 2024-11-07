package com.example.sizeestimator.domain.bitmap.drawer

import android.graphics.Color
import com.example.sizeestimator.domain.MeasurementTrace
import com.example.sizeestimator.domain.bitmap.LoresBitmap

/**
 * Something that knows how to draw some aspect of a [MeasurementTrace] data set into a [LoresBitmap]
 */
interface MTDrawer {
    /**
     * @param bitmap Bitmap to draw on
     * @param data to be drawn into the bitmap
     */
    fun draw(bitmap: LoresBitmap, trace: MeasurementTrace)

    /**
     * The next MTDrawer object that will be given the chance to draw.
     *
     * @property next the next element in the chain of MTDrawer objects, or null if this is the
     * last MTDrawer object in the chain.
     */
    val next: MTDrawer?

    companion object {
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