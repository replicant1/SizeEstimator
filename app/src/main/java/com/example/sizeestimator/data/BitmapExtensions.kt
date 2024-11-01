package com.example.sizeestimator.data

import android.content.Context
import android.graphics.Bitmap
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream

/**
 * @return The bitmaps filename, or null if couldn't save
 */
fun Bitmap.saveToAppCache(context: Context, filename: String) {
    val dir = context.cacheDir

    if (!dir.exists()) {
        dir.mkdir()
    }

    val path = dir.absolutePath + File.separator + filename

    Timber.d("About to save bitmap to cache file:$path")

    try {
        val fileOutputStream = FileOutputStream(path)
        this.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
        fileOutputStream.close()

        Timber.d("Wrote bitmap OK to cache file $path")

    } catch (e: java.lang.Exception) {
        Timber.w(e)
    }
}

/**
 * Scale and crop to square
 */
fun Bitmap.toSquare(side: Int): Bitmap {
    Timber.d("Cropping bitmap to side = $side")

    // Cropping this much off width should make image square
    // NOTE: Assuming width > height
    val horizontalCrop = (this.width - this.height) / 2
    val squaredBitmap = Bitmap.createBitmap(
        this,
        horizontalCrop,
        0,
        this.height,
        this.height
    )

    Timber.d("Squared bitmap has width = ${squaredBitmap.width} + height = ${squaredBitmap.height}")

    // Scale down to size expected by TensorFlow model
    val scaledSquareBitmap = Bitmap.createScaledBitmap(
        squaredBitmap, side, side, false
    )
    Timber.d("Scaled square bitmap has width = ${scaledSquareBitmap.width}, height = ${scaledSquareBitmap.height}")

    return scaledSquareBitmap
}