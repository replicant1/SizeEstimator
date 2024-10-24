package com.example.sizeestimator

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.annotation.VisibleForTesting
import com.example.sizeestimator.MainActivity.Companion.ANALYSED_IMAGE_DIR
import com.example.sizeestimator.ml.SsdMobilenetV1
import org.tensorflow.lite.support.image.TensorImage
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class AnalysableBitmapFile(private val bitmapFile: File) {


    companion object {

        /**
         * Static factory method. Create the lo-res equivalent of the raw camera image that can
         * be analysed and processed.
         *
         * @param cameraBitmapFile raw image produced by the camera. Assumed to be in landscape orientation.
         * @return AnalysableBitmapFile that represents the given [cameraBitmapFile] transformed into
         * a size and format suitable for TensorFlow processing. Will have dimensions [TENSOR_FLOW_IMAGE_SIZE_PX]
         */




        private val TAG = AnalysableBitmapFile::class.java.simpleName

        val RECT_COLORS: List<Int> =
            listOf(
                Color.RED,
                Color.YELLOW,
                Color.BLUE,
                Color.CYAN,
                Color.BLACK,
                Color.DKGRAY,
                Color.GRAY,
                Color.GREEN,
                Color.LTGRAY,
                Color.MAGENTA
            )

    }

}