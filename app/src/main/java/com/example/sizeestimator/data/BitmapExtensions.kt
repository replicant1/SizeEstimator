package com.example.sizeestimator.data

import android.graphics.Bitmap
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream

/**
 * @param dir the directory to save this bitmap as jpeg
 * @param filename simple name of file within [dir]
 */
fun Bitmap.save(dir: File, filename: String) {
    if (!dir.exists()) {
        dir.mkdir()
    }

    val path = dir.absolutePath + File.separator + filename

    Timber.d("About to save bitmap to $path")

    try {
        val fileOutputStream = FileOutputStream(path)
        this.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
        fileOutputStream.close()

        Timber.d("Wrote bitmap OK to $path")

    } catch (e: Exception) {
        Timber.w(e)
    }
}

/**
 * Scale and crop to square
 */
fun Bitmap.toSquare(side: Int): Bitmap {
    Timber.d("Cropping bitmap to side = $side px")

    // Cropping this much off width should make image square
    // NOTE: Assuming width > height
    val squaredBitmap = if (this.width >= this.height) {
        val horizontalCrop = (this.width - this.height) / 2
        Bitmap.createBitmap(
            this, // source
            horizontalCrop, // x
            0, // y
            this.height, // width
            this.height  // height
        )
    } else {
        val verticalCrop = (this.height - this.width) / 2
        Bitmap.createBitmap(
            this, // source
            0, // x
            verticalCrop, // y
            this.width, // width
            this.width // height
        )
    }

    Timber.d("Squared bitmap has width = ${squaredBitmap.width}, height = ${squaredBitmap.height}")

    // Scale down to desired size
    val scaledSquareBitmap = Bitmap.createScaledBitmap(
        squaredBitmap, side, side, false
    )
    Timber.d("Scaled square bitmap has width = ${scaledSquareBitmap.width}, height = ${scaledSquareBitmap.height}")

    return scaledSquareBitmap
}