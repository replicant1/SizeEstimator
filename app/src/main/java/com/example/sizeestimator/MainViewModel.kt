package com.example.sizeestimator

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import androidx.camera.core.ImageCapture
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.sizeestimator.LoresBitmap.AnalysisOptions
import dagger.hilt.android.lifecycle.HiltViewModel

@HiltViewModel
class MainViewModel : ViewModel() {

    val _sizeText = MutableLiveData<String>()
    val sizeText: LiveData<String>
        get() = _sizeText

    fun onImageSaved(
        tempFilePath: String,
        output: ImageCapture.OutputFileResults,
        context: Context
    ) {
        Log.d(TAG, "** tempFilePath = $tempFilePath")
        Log.d(TAG, "** output.savedUri = ${output.savedUri}")
        Log.d(TAG, "About to crop photo to size expected by tensor flow model")
        val cameraImage = BitmapFactory.decodeFile(tempFilePath)
        Log.d(
            TAG,
            "Camera image: width=${cameraImage.width}, height=${cameraImage.height}"
        )

        val loresBitmap = LoresBitmap.fromHiresBitmap(cameraImage)

        if (loresBitmap != null) {
            Log.d(TAG, "Analysing the lores image")
            val result = loresBitmap.analyse(
                context,
                AnalysisOptions(LoresBitmap.LORES_IMAGE_SIZE_PX / 2F) // vertical midpoint
            )

            Log.d(TAG, "Add the bounding boxes and legend to the lores image")
            loresBitmap.markup(result)

            // Save bitmap
            loresBitmap.save(context.cacheDir, LORES_FILENAME)

            // Put result on screen
            _sizeText.value =
                "Size: ${result.targetObjectSizeMillimetres.first} x ${result.targetObjectSizeMillimetres.second} mm"
        } else {
            Log.d(TAG, "Failed to crop photo to size expected by tensor flow model")
        }
    }

    companion object {
        private val TAG = MainViewModel::class.java.simpleName
        private const val LORES_FILENAME = "lores.jpg"
    }
}