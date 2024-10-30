package com.example.sizeestimator.presentation

import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable

@Composable
fun SizeEstimatorScreen(
    sizeText: String?,
    progressMonitorVisible: Boolean?,
    onButtonClick: () -> Unit
) {
    Row {
        CameraPreview()
        MeasureButtonPanel(sizeText, progressMonitorVisible, onButtonClick)
    }

}