package com.example.sizeestimator.domain

import android.util.Size

/** @see Analyser.analyse */
data class AnalysisResult(
    val sortedResults: SortedResultList,
    val referenceObject: TestableDetectionResult,
    val targetObject: TestableDetectionResult,
    val targetObjectSizeMillimetres: Pair<Int, Int>
)