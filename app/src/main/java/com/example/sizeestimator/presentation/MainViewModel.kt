package com.example.sizeestimator.presentation

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.sizeestimator.data.LoresBitmap
import com.example.sizeestimator.data.LoresBitmap.AnalysisOptions
import dagger.hilt.android.lifecycle.HiltViewModel

@HiltViewModel
class MainViewModel : ViewModel() {

    // eg. "90 x 45 mm"
    val _sizeText = MutableLiveData<String>()
    val sizeText: LiveData<String>
        get() = _sizeText

    /**
     * Invoked when hires camera image has been saved to app's cache directory and we are now
     * ready to analyse it to give a measurement of the target object.
     */
    fun onHiresImageSaved(
        hiresPath: String,
        context: Context
    ) {
        Log.d(TAG, "About to crop photo to size expected by tensor flow model")
        val hiresBitmap = BitmapFactory.decodeFile(hiresPath)
        Log.d(
            TAG,
            "Camera image: width=${hiresBitmap.width}, height=${hiresBitmap.height}"
        )

        val loresBitmap = LoresBitmap.fromHiresBitmap(hiresBitmap)

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
                "${result.targetObjectSizeMillimetres.first} x ${result.targetObjectSizeMillimetres.second} mm"
        } else {
            Log.d(TAG, "Failed to crop photo to size expected by tensor flow model")
        }
    }

    companion object {
        private val TAG = MainViewModel::class.java.simpleName
        private const val LORES_FILENAME = "lores.jpg"
    }
}