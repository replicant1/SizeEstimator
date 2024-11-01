package com.example.sizeestimator

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.sizeestimator.data.toSquare
import org.junit.Assert

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    //    @Test
//    fun useAppContext() {
//        // Context of the app under test.
//        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
//        assertEquals("com.example.sizeestimator", appContext.packageName)
//    }
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
        assertEquals(300, square.width)
        assertEquals(300, square.height)
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
        val square = portrait.toSquare(300)
        assertEquals(300, square.width)
        assertEquals(300, square.height)
    }
}