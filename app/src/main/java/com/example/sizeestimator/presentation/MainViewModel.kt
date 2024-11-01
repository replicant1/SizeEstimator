package com.example.sizeestimator.presentation

import android.content.Context
import android.graphics.BitmapFactory
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sizeestimator.BuildConfig
import com.example.sizeestimator.data.LoresBitmap
import com.example.sizeestimator.data.LoresBitmap.Companion.LORES_IMAGE_SIZE_PX
import com.example.sizeestimator.data.saveToAppCache
import com.example.sizeestimator.domain.MeasurementEngine.Companion.measure
import com.example.sizeestimator.domain.MeasurementEngine.MeasurementOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

@HiltViewModel
class MainViewModel : ViewModel() {

    // eg. "90 x 45 mm"
    val _sizeText = MutableLiveData<String>()
    val sizeText: LiveData<String>
        get() = _sizeText

    // Is the progress monitor showing
    val progressMonitorVisible = MutableLiveData(false)

    private var errorChannel = Channel<String>()
    var errorFlow = errorChannel.receiveAsFlow()

    fun onError(message: String) {
        viewModelScope.launch {
            errorChannel.send(message)
            Timber.e(message)
        }
    }

    /**
     * Invoked when hires camera image has been saved to app's cache directory and we are now
     * ready to analyse it to give a measurement of the target object.
     */
    fun onHiresImageSaved(
        hiresPath: String,
        context: Context
    ) {
        viewModelScope.launch(Dispatchers.Default) {
            withContext(Dispatchers.Main) {
                progressMonitorVisible.value = true
            }

            // Get the raw camera image
            Timber.d("Retrieving raw camera image")
            val hiresBitmap = BitmapFactory.decodeFile(hiresPath)
            if (BuildConfig.DEBUG) {
                hiresBitmap.saveToAppCache(context, HIRES_FILENAME)
            }

            // Crop and scale camera image to size Tensor Flow model expects
            Timber.d("Scaling and cropping camera image to tensor flow size")
            val loresBitmap = LoresBitmap.fromHiresBitmap(hiresBitmap)
            if (BuildConfig.DEBUG) {
                loresBitmap.saveToAppCache(context, LORES_NO_LEGEND_FILENAME)
            }

            // Apply Tensor Flow model and subsequent processing
            Timber.d("Apply Tensor Flow and other algorithms to measure target object")
            val scoreboard = loresBitmap.score(context)
            val trace = measure(scoreboard, MeasurementOptions(minTop = LORES_IMAGE_SIZE_PX / 2f))

            if ((trace != null) && BuildConfig.DEBUG) {
                // Save small image marked up with legend etc for debugging
                loresBitmap.drawTrace(trace)
                loresBitmap.saveToAppCache(context, LORES_MARKED_UP_FILENAME)
            } else {
                Timber.d("Failed to measure target object")
            }

            // Put measurement results on screen
            withContext(Dispatchers.Main) {
                _sizeText.value =
                    "${trace?.targetObjectSizeMm?.first} x ${trace?.targetObjectSizeMm?.second} mm"
                Timber.d("Changing size text to ${_sizeText.value}")
            }

            withContext(Dispatchers.Main) {
                progressMonitorVisible.value = false
            }
        }
    }

    companion object {
        private const val LORES_MARKED_UP_FILENAME = "lores_marked_up.jpg"
        private const val LORES_NO_LEGEND_FILENAME = "lores.jpg"
        const val HIRES_FILENAME = "hires.jpg"
    }
}