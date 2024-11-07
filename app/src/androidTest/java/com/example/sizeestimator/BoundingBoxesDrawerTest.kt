package com.example.sizeestimator

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.sizeestimator.domain.bitmap.drawer.BoundingBoxesDrawer
import com.example.sizeestimator.domain.bitmap.LoresBitmap
import com.example.sizeestimator.domain.bitmap.boxMatches
import com.example.sizeestimator.domain.bitmap.pixelMatches
import com.example.sizeestimator.domain.scoreboard.BoundingBox
import com.example.sizeestimator.domain.MeasurementTrace
import com.example.sizeestimator.domain.scoreboard.Scoreboard
import com.example.sizeestimator.domain.scoreboard.ScoreboardItem
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BoundingBoxesDrawerTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
    }

    @Test
    fun loadAssetImageAndDrawBoxes() {
        // Create LoresBitma from empty bitmap - we won't be analysing it anyway.
        val lores = LoresBitmap.fromHiresBitmap(Bitmap.createBitmap(400, 400, Bitmap.Config.RGB_565))
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
        BoundingBoxesDrawer(BoundingBoxesDrawer.BoundingBoxStyle.FILL, null).draw(lores, trace)

        Assert.assertTrue(
            lores.squareBitmap.pixelMatches(150, 150, Color.Red.toArgb()))
        Assert.assertTrue(
            lores.squareBitmap.boxMatches(
                BoundingBox(top =10f, left = 10f, right = 50f, bottom = 50f),
            Color.Red.toArgb()))
    }
}



