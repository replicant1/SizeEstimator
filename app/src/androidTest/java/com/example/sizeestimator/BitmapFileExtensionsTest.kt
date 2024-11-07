package com.example.sizeestimator

import android.content.Context
import android.graphics.Bitmap
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.sizeestimator.data.save
import org.junit.Assert
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class BitmapFileExtensionsTest {

    private lateinit var context : Context

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
    }

    @Test
    fun save() {
        val bitmap = Bitmap.createBitmap(100, 200, Bitmap.Config.RGB_565)
        bitmap.save(context.filesDir, "bob.jpg")
        val file = File(context.filesDir, "bob.jpg")
        assertTrue(file.exists())
    }

    @Test
    fun saveOnTopOfSame() {
        val bitmap = Bitmap.createBitmap(100, 200, Bitmap.Config.RGB_565)
        bitmap.save(context.filesDir, "acme.jpg")
        bitmap.save(context.filesDir, "acme.jpg")

        val file = File(context.filesDir, "acme.jpg")
        assertTrue(file.exists())
    }

}