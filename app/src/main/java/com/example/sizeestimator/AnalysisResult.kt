package com.example.sizeestimator

import com.example.sizeestimator.ml.SsdMobilenetV1


data class AnalysisResult(
    val sortedResults: List<SsdMobilenetV1.DetectionResult>,
    val referenceObjectIndex: Int,
    val targetObjectIndex: Int,
    val targetObjectSizeMillimetres: Pair<Long, Long>
)