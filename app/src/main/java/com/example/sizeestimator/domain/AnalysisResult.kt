package com.example.sizeestimator.domain

/** @see Analyser.analyse */
data class AnalysisResult(
    val sortedResults: List<TestableDetectionResult>,
    val referenceObjectIndex: Int,
    val targetObjectIndex: Int,
    val targetObjectSizeMillimetres: Pair<Long, Long>
)