package com.example.sizeestimator

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.sizeestimator.data.BoundingBoxesDrawer
import com.example.sizeestimator.data.BoundingBoxesDrawer.BoundingBoxStyle.FILL
import com.example.sizeestimator.data.LegendDrawer
import com.example.sizeestimator.data.LegendDrawer.Companion.LEGEND_BOX_WIDTH_PX
import com.example.sizeestimator.data.LegendDrawer.Companion.LEGEND_MARGIN_PX
import com.example.sizeestimator.data.LegendDrawer.Companion.LEGEND_ROW_HEIGHT_PX
import com.example.sizeestimator.data.LoresBitmap
import com.example.sizeestimator.data.MTDrawer
import com.example.sizeestimator.data.pixelMatches
import com.example.sizeestimator.domain.BoundingBox
import com.example.sizeestimator.domain.MeasurementEngine
import com.example.sizeestimator.domain.Scoreboard
import com.example.sizeestimator.domain.ScoreboardItem
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.InputStream

@RunWith(AndroidJUnit4::class)
class LegendDrawerTest {
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
    }

    @Test
    fun loadAssetImageAndDrawLegend() {
        val fis: InputStream = context.assets.open(TestAssets.RAW_CAMERA_IMAGE)
        val immutable = BitmapFactory.decodeStream(fis)
        val mutable = immutable.copy(Bitmap.Config.ARGB_8888, true)
        val lores = LoresBitmap.fromHiresBitmap(mutable)
        val scoreboard = lores.score(context)
        val trace = MeasurementEngine.measure(
            scoreboard,
            MeasurementEngine.MeasurementOptions(minTop = LoresBitmap.LORES_IMAGE_SIZE_PX / 2f)
        )
        if (trace != null) {
            assertNotNull(trace)
            assertNotNull(trace.referenceObject)
            assertNotNull(trace.targetObject)
            assertNotNull(trace.targetObjectSizeMm)
            assertNotNull(trace.scoreboard)

            LegendDrawer().draw(lores, trace)
            assertEquals(LoresBitmap.LORES_IMAGE_SIZE_PX, lores.squareBitmap.width)
            assertEquals(LoresBitmap.LORES_IMAGE_SIZE_PX, lores.squareBitmap.height)

            assertFalse(immutable.sameAs(lores.squareBitmap))

            // Test the first box in the legend is drawn at expected location
            assertTrue(legendBoxMatches(lores.squareBitmap, 0))

            // Test the last box in the legend is drawn at expected location
            assertTrue(legendBoxMatches(lores.squareBitmap, MTDrawer.MARK_UP_COLORS.lastIndex))
        }
    }

    private fun legendBoxMatches(bitmap: Bitmap, index: Int): Boolean {
        val color = MTDrawer.MARK_UP_COLORS[index]
        val halfBox = (LEGEND_BOX_WIDTH_PX / 2f)
        val boxCenterX = LEGEND_MARGIN_PX + halfBox
        val boxCenterY = LEGEND_MARGIN_PX + (index * LEGEND_ROW_HEIGHT_PX) + halfBox
        return bitmap.pixelMatches(boxCenterX.toInt(), boxCenterY.toInt(), color)
    }
}