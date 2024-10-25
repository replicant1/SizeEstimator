package com.example.sizeestimator.domain

import com.example.sizeestimator.ml.SsdMobilenetV1


data class AnalysisResult(
    val sortedResults: List<TestableDetectionResult>,
    val referenceObjectIndex: Int,
    val targetObjectIndex: Int,
    val targetObjectSizeMillimetres: Pair<Long, Long>
)