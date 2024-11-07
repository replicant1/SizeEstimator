package com.example.sizeestimator

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.sizeestimator.data.LoresBitmap
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.InputStream

@RunWith(AndroidJUnit4::class)
class LoresBitmapTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
    }

    @Test
    fun scaleDownDynamicallyCreatedBitmapAndSaveAndReloadFromFile() {
        val hires = Bitmap.createBitmap(400, 500, Bitmap.Config.RGB_565)

        // Save scaled down bitmap as "test.jpg"
        val lores = LoresBitmap.fromHiresBitmap(hires)
        lores.save(context.filesDir, "test.jpg")

        val path = context.filesDir.path + File.separator + "test.jpg"
        val input = BitmapFactory.decodeFile(path)

        // Check that "test.jpg" is 300x300
        assertEquals(LoresBitmap.LORES_IMAGE_SIZE_PX, input.width)
        assertEquals(LoresBitmap.LORES_IMAGE_SIZE_PX, input.height)
    }

    @Test
    fun scaledDownAssetImageAndScoreWithTensorFlowModel() {
        // Load "hires.jpg" from assets folder
        val fis: InputStream = context.assets.open(TestAssets.RAW_CAMERA_IMAGE)
        val hiresBitmap = BitmapFactory.decodeStream(fis)
        assertEquals(4080, hiresBitmap.width)
        assertEquals(3072, hiresBitmap.height)

        // Convert hires to lores in memory
        val lores = LoresBitmap.fromHiresBitmap(hiresBitmap)

        // Apply ML algorithms to find bounding boxes and scores
        val scoreboard = lores.score(context)
        assertEquals(10, scoreboard.list.size)
        println(scoreboard.list)
        assertEquals(0.59765625.toFloat(), scoreboard.list[0].score)
        assertEquals(0.3671875.toFloat(), scoreboard.list[1].score)
    }
}