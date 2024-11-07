package com.example.sizeestimator

import android.graphics.Bitmap
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.sizeestimator.domain.bitmap.LoresBitmap
import com.example.sizeestimator.domain.bitmap.toSquare
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class BitmapExtensionsTest {

    @Test
    fun landscapeToSmallerSquare() {
        val landscape = Bitmap.createBitmap(200, 100, Bitmap.Config.RGB_565)
        val square = landscape.toSquare(50)
        assertEquals(50, square.width)
        assertEquals(50, square.height)
    }

    @Test
    fun landscapeToLargerSquare() {
        val landscape = Bitmap.createBitmap(200, 100, Bitmap.Config.RGB_565)
        val square = landscape.toSquare(300)
        assertEquals(LoresBitmap.LORES_IMAGE_SIZE_PX, square.width)
        assertEquals(LoresBitmap.LORES_IMAGE_SIZE_PX, square.height)
    }

    @Test
    fun portraitToSmallerSquare() {
        val portrait = Bitmap.createBitmap(100, 200, Bitmap.Config.RGB_565)
        val square = portrait.toSquare(50)
        assertEquals(50, square.width)
        assertEquals(50, square.height)
    }

    @Test
    fun portraitToLargerSquare() {
        val portrait = Bitmap.createBitmap(100, 200, Bitmap.Config.RGB_565)
        val square = portrait.toSquare(LoresBitmap.LORES_IMAGE_SIZE_PX)
        assertEquals(LoresBitmap.LORES_IMAGE_SIZE_PX, square.width)
        assertEquals(LoresBitmap.LORES_IMAGE_SIZE_PX, square.height)
    }
}