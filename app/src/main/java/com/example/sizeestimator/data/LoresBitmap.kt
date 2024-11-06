package com.example.sizeestimator.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Typeface
import com.example.sizeestimator.domain.MeasurementTrace
import com.example.sizeestimator.domain.Scoreboard
import com.example.sizeestimator.domain.toScoreboardItemList
import com.example.sizeestimator.ml.SsdMobilenetV1
import org.tensorflow.lite.support.image.TensorImage
import java.io.File

/**
 * A small bitmap that has been scaled down and cropped from a raw camera image, and is small enough
 * for the Tensor Flow model to process.
 * @property squareBitmap a bitmap that has been cropped and scaled to have width and height
 */
class LoresBitmap private constructor(public var squareBitmap: Bitmap) {
    companion object {
        /**
         * Factory method to create a LoresBitmap
         */
        fun fromHiresBitmap(hiresBitmap: Bitmap): LoresBitmap {
            return LoresBitmap(hiresBitmap.toSquare(LORES_IMAGE_SIZE_PX))
        }

        const val LORES_IMAGE_SIZE_PX = 300
    }

    fun save(dir: File, filename: String) {
        squareBitmap.save(dir, filename)
    }

    /**
     * Apply Tensor Flow Lite model to the picture passed into the constructor,
     * to find bounding boxes of objects in the picture and the confidence of each.
     */
    fun score(context: Context): Scoreboard {
        val model = SsdMobilenetV1.newInstance(context)
        val image = TensorImage.fromBitmap(squareBitmap)
        val outputs = model.process(image)
        model.close()
        return Scoreboard(outputs.detectionResultList.toScoreboardItemList())
    }

    /**
     * Draw some aspect of [trace] into this [squareBitmap].
     */
    fun draw(drawer : MTDrawer, trace:MeasurementTrace) {
        drawer.draw(this, trace)
    }
}