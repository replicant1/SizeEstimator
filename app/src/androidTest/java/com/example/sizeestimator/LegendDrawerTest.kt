package com.example.sizeestimator

import android.content.Context
import android.graphics.Bitmap
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.sizeestimator.domain.bitmap.drawer.LegendDrawer
import com.example.sizeestimator.domain.bitmap.drawer.LegendDrawer.Companion.LEGEND_BOX_WIDTH_PX
import com.example.sizeestimator.domain.bitmap.drawer.LegendDrawer.Companion.LEGEND_MARGIN_PX
import com.example.sizeestimator.domain.bitmap.drawer.LegendDrawer.Companion.LEGEND_ROW_HEIGHT_PX
import com.example.sizeestimator.domain.bitmap.LoresBitmap
import com.example.sizeestimator.domain.bitmap.drawer.MTDrawer
import com.example.sizeestimator.domain.bitmap.pixelMatches
import com.example.sizeestimator.domain.scoreboard.BoundingBox
import com.example.sizeestimator.domain.MeasurementTrace
import com.example.sizeestimator.domain.scoreboard.Scoreboard
import com.example.sizeestimator.domain.scoreboard.ScoreboardItem
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LegendDrawerTest {
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
    }

    @Test
    fun loadAssetImageAndDrawLegend() {
        // Create LoresBitmap from empty bitmap - we won't be analysing it anyway.
        val lores =
            LoresBitmap.fromHiresBitmap(Bitmap.createBitmap(400, 400, Bitmap.Config.RGB_565))
        val scoreboard = Scoreboard(
            mutableListOf(
                ScoreboardItem(
                    score = 10f,
                    location = BoundingBox(top = 100f, left = 100f, right = 120f, bottom = 145f)
                ),
                ScoreboardItem(
                    score = 9f,
                    location = BoundingBox(top = 100f, left=100f, right = 120f, bottom = 130f)
                )
            )
        )

        val trace = MeasurementTrace(
            scoreboard = scoreboard,
            referenceObject = scoreboard.list.first(),
            targetObject = scoreboard.list.first(),
            targetObjectSizeMm = Pair(0, 0)
        )

        var nextDrawMethodCalled = false
        LegendDrawer(
            object : MTDrawer {
                override fun draw(bitmap: LoresBitmap, trace: MeasurementTrace) {
                    nextDrawMethodCalled = true
                }

                override val next: MTDrawer? = null
            }
        ).draw(lores, trace)

        assertTrue(nextDrawMethodCalled)
        assertEquals(LoresBitmap.LORES_IMAGE_SIZE_PX, lores.squareBitmap.width)
        assertEquals(LoresBitmap.LORES_IMAGE_SIZE_PX, lores.squareBitmap.height)

        // Test the first box in the legend is drawn at expected location
        assertTrue(legendBoxMatches(lores.squareBitmap, 0))

        // Test the last box in the legend is drawn at expected location
        assertTrue(legendBoxMatches(lores.squareBitmap, 1))
    }

    private fun legendBoxMatches(bitmap: Bitmap, index: Int): Boolean {
        val color = MTDrawer.MARK_UP_COLORS[index]
        val halfBox = (LEGEND_BOX_WIDTH_PX / 2f)
        val boxCenterX = LEGEND_MARGIN_PX + halfBox
        val boxCenterY = LEGEND_MARGIN_PX + (index * LEGEND_ROW_HEIGHT_PX) + halfBox
        return bitmap.pixelMatches(boxCenterX.toInt(), boxCenterY.toInt(), color)
    }
}