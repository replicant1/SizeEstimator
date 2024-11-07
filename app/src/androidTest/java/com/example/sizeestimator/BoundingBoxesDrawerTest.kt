package com.example.sizeestimator

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.sizeestimator.data.BoundingBoxesDrawer
import com.example.sizeestimator.data.LoresBitmap
import com.example.sizeestimator.data.boxMatches
import com.example.sizeestimator.data.pixelMatches
import com.example.sizeestimator.domain.BoundingBox
import com.example.sizeestimator.domain.MeasurementTrace
import com.example.sizeestimator.domain.Scoreboard
import com.example.sizeestimator.domain.ScoreboardItem
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.InputStream

@RunWith(AndroidJUnit4::class)
class BoundingBoxesDrawerTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
    }

    @Test
    fun loadAssetImageAndDrawBoxes() {
        val fis: InputStream = context.assets.open(TestAssets.RAW_CAMERA_IMAGE)
        val immutable = BitmapFactory.decodeStream(fis)
        val mutable = immutable.copy(Bitmap.Config.ARGB_8888, true)
        val lores = LoresBitmap.fromHiresBitmap(mutable)
        val scoreboard = Scoreboard(
            mutableListOf(
                ScoreboardItem(
                    score = 10f,
                    location = BoundingBox(top = 0f, left = 0f, right = 299f, bottom = 299f)
                )
            )
        )

        // Can't get mockk going with instrumented tests.
        val trace = MeasurementTrace(
            scoreboard = scoreboard,
            referenceObject = scoreboard.list.first(), // Not consulted by BoundingBoxesDrawer
            targetObject = scoreboard.list.first(), // Not consulted by BoundingBoxesDrawer
            targetObjectSizeMm = Pair(0, 0) // Not consulted by BoundingBoxesDrawer
        )

        // Draw the bounding boxes
        BoundingBoxesDrawer(BoundingBoxesDrawer.BoundingBoxStyle.FILL).draw(lores, trace)

        Assert.assertTrue(
            lores.squareBitmap.pixelMatches(150, 150, Color.Red.toArgb()))
        Assert.assertTrue(
            lores.squareBitmap.boxMatches(BoundingBox(top =10f, left = 10f, right = 50f, bottom = 50f),
            Color.Red.toArgb()))
    }
}



