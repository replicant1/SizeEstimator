package com.example.sizeestimator.presentation

import android.content.Context
import android.graphics.BitmapFactory
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sizeestimator.data.LoresBitmap
import com.example.sizeestimator.data.LoresBitmap.AnalysisOptions
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
        progressMonitorVisible.value = true

        viewModelScope.launch(Dispatchers.Default) {
            Timber.d("About to crop photo to size expected by tensor flow model")
            val hiresBitmap = BitmapFactory.decodeFile(hiresPath)
            Timber.d("Camera image: width=${hiresBitmap.width}, height=${hiresBitmap.height}")

            val loresBitmap = LoresBitmap.fromHiresBitmap(hiresBitmap)

            if (loresBitmap != null) {
                Timber.d("Analysing the lores image")
                val result = loresBitmap.analyse(
                    context,
                    AnalysisOptions(LoresBitmap.LORES_IMAGE_SIZE_PX / 2F) // vertical midpoint
                )

                if (result != null) {
                    Timber.d("Add the bounding boxes and legend to the lores image")
                    loresBitmap.markup(result)
                }

                // Save bitmap
                loresBitmap.save(context.cacheDir, LORES_FILENAME)

                // Put result on screen
                if (result != null) {
                    withContext(Dispatchers.Main) {
                        _sizeText.value =
                            "${result.targetObjectSizeMillimetres.first} x ${result.targetObjectSizeMillimetres.second} mm"
                        Timber.d("Changing size text to ${_sizeText.value}")
                    }
                }
            } else {
                errorChannel.send("Failed to process image")
                Timber.d("Failed to crop photo to size expected by tensor flow model")
            }

            withContext(Dispatchers.Main) {
                progressMonitorVisible.value = false
            }
        }
    }

    companion object {
        const val LORES_FILENAME = "lores.jpg"
        const val HIRES_FILENAME = "hires.jpg"
    }
}