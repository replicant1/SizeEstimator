package com.example.sizeestimator

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.sizeestimator.data.LegendDrawer
import com.example.sizeestimator.data.LegendDrawer.Companion.LEGEND_BOX_WIDTH_PX
import com.example.sizeestimator.data.LegendDrawer.Companion.LEGEND_ROW_HEIGHT_PX
import com.example.sizeestimator.data.LoresBitmap
import com.example.sizeestimator.data.MTDrawer
import com.example.sizeestimator.domain.MeasurementEngine
import org.junit.Assert
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.InputStream
import com.example.sizeestimator.data.LegendDrawer.Companion.LEGEND_MARGIN_PX as LEGEND_MARGIN_PX1

@RunWith(AndroidJUnit4::class)
class BoundingBoxesDrawerTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
    }

    @Test
    fun loadAssetImageAndDrawLegend() {
        val fis: InputStream = context.assets.open("hires.jpg")
        val immutable = BitmapFactory.decodeStream(fis)
        val mutable = immutable.copy(Bitmap.Config.ARGB_8888, true)
        val lores = LoresBitmap.fromHiresBitmap(mutable)
        val scoreboard = lores.score(context)
        scoreboard.list.forEach {
            println("score = ${it.score} , location = ${it.location}")
        }
        val trace = MeasurementEngine.measure(
            scoreboard,
            MeasurementEngine.MeasurementOptions(minTop = 150f)
        )
        if (trace != null) {
            Assert.assertNotNull(trace)
            Assert.assertNotNull(trace.referenceObject)
            Assert.assertNotNull(trace.targetObject)
            Assert.assertNotNull(trace.targetObjectSizeMm)
            Assert.assertNotNull(trace.scoreboard)

            LegendDrawer().draw(lores, trace)
            Assert.assertEquals(LoresBitmap.LORES_IMAGE_SIZE_PX, lores.squareBitmap.width)
            Assert.assertEquals(LoresBitmap.LORES_IMAGE_SIZE_PX, lores.squareBitmap.height)

            Assert.assertFalse(immutable.sameAs(lores.squareBitmap))

            // Test the first box in the legend is drawn at expected location
            assertTrue(legendBoxMatches(lores.squareBitmap, 0))

            // Test the last box in the legend is drawn at expected location
            assertTrue(legendBoxMatches(lores.squareBitmap, MTDrawer.MARK_UP_COLORS.lastIndex))
        }
    }

    fun legendBoxMatches(bitmap: Bitmap, index: Int): Boolean {
        val color = MTDrawer.MARK_UP_COLORS[index]
        val halfBox = (LEGEND_BOX_WIDTH_PX / 2f)
        val boxCenterX = LEGEND_MARGIN_PX1 + halfBox
        val boxCenterY = LEGEND_MARGIN_PX1 + (index * LEGEND_ROW_HEIGHT_PX) + halfBox
        return bitmap.pixelMatches(boxCenterX.toInt(), boxCenterY.toInt(), color)
    }
}

fun Color.matches(otherColor: Int) : Boolean {
    val colorToMatch = Color.valueOf(otherColor)
    val redMatch = colorToMatch.red() == red()
    val greenMatch = colorToMatch.green() == green()
    val blueMatch = colorToMatch.blue() == blue()
    return redMatch && greenMatch && blueMatch
}

fun Bitmap.pixelMatches(x : Int, y : Int, color: Int) : Boolean {
    val thisColor : Color = this.getColor(x, y)
    return thisColor.matches(color)
}