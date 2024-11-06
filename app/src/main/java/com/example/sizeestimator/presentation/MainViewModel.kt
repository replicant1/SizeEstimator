package com.example.sizeestimator.presentation

import android.content.Context
import android.graphics.BitmapFactory
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sizeestimator.BuildConfig
import com.example.sizeestimator.data.BoundingBoxesDrawer
import com.example.sizeestimator.data.LegendDrawer
import com.example.sizeestimator.data.LoresBitmap
import com.example.sizeestimator.data.save
import com.example.sizeestimator.domain.MeasurementEngine
import com.example.sizeestimator.domain.MeasurementTrace
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
    private val _sizeText = MutableLiveData<String>()
    val sizeText: LiveData<String>
        get() = _sizeText

    // Is the progress monitor showing
    private val _progressMonitorVisible = MutableLiveData(false)
    val progressMonitorVisible: LiveData<Boolean>
        get() = _progressMonitorVisible

    private val _measurementTrace = MutableLiveData<MeasurementTrace?>()
    val measurementTrace: LiveData<MeasurementTrace?>
        get() = _measurementTrace

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
        _progressMonitorVisible.value = true
        viewModelScope.launch(Dispatchers.Default) {
            withContext(Dispatchers.Main) {
                _progressMonitorVisible.value = true
            }
        }

        viewModelScope.launch(Dispatchers.Default) {
            Timber.d("About to crop photo to size expected by tensor flow model")
            val hiresBitmap = BitmapFactory.decodeFile(hiresPath)
            if (BuildConfig.DEBUG) {
                hiresBitmap.save(context.cacheDir, HIRES_FILENAME)
            }

            // Crop and scale camera image to size Tensor Flow model expects and save for debugging
            Timber.d("Scaling and cropping camera image to tensor flow size")
            val loresBitmap = LoresBitmap.fromHiresBitmap(hiresBitmap)
            if (BuildConfig.DEBUG) {
                loresBitmap.save(context.cacheDir, LORES_NO_LEGEND_FILENAME)
            }

            // Apply Tensor Flow model and subsequent processing
            Timber.d("Apply Tensor Flow and other algorithms to measure target object")
            val scoreboard = loresBitmap.score(context)
            val trace = MeasurementEngine.measure(
                scoreboard,
                MeasurementEngine.MeasurementOptions(minTop = LoresBitmap.LORES_IMAGE_SIZE_PX / 2f)
            )

            if (trace != null) {
                withContext(Dispatchers.Main) {
                    _measurementTrace.value = trace
                }

                if (BuildConfig.DEBUG) {
                    // Save small image marked up with legend etc for debugging
                    LegendDrawer().draw(loresBitmap, trace)
                    BoundingBoxesDrawer().draw(loresBitmap, trace)
                    loresBitmap.save(context.cacheDir, LORES_MARKED_UP_FILENAME)
                }
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
                _progressMonitorVisible.value = false
            }
        }
    }

    companion object {
        private const val LORES_MARKED_UP_FILENAME = "lores_marked_up.jpg"
        private const val LORES_NO_LEGEND_FILENAME = "lores.jpg"
        const val HIRES_FILENAME = "hires.jpg"
    }
}