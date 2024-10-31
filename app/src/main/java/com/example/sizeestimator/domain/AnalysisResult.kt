package com.example.sizeestimator.domain

import android.util.Size

/** @see Analyser.analyse */
data class AnalysisResult(
    val sortedResults: SortedResultList,
    val referenceObjectIndex: Int,
    val targetObjectIndex: Int,
    val targetObjectSizeMillimetres: Pair<Int, Int>
)