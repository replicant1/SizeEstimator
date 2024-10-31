package com.example.sizeestimator.domain

import android.util.Size
import com.example.sizeestimator.data.LoresBitmap
import timber.log.Timber

/**
 * Analyses the output of the Tensor Flow model and deduces an estimate for the
 * actual size of the target object.
 * @param sortedResults output of the Tensor Flow model - bounding boxes with scores
 */
class Analyser(private val sortedResults: SortedResultList) {

    /**
     * Analyse the Tensor Flow results provided to constructor.
     */
    fun analyse(options: LoresBitmap.AnalysisOptions): AnalysisResult? {
        var result: AnalysisResult? = null

        val analysisTime = kotlin.time.measureTime {
            val referenceObjectIndex =
                sortedResults.process(ReferenceObjectFinder(options.minTop)) as Int?
            if (referenceObjectIndex != null) {
                val targetObjectIndex =
                    sortedResults.process(TargetObjectFinder(referenceObjectIndex)) as Int?
                if (targetObjectIndex != null) {
                    val targetObjectSizeMm = sortedResults.process(
                        ObjectSizer(referenceObjectIndex, targetObjectIndex)) as Pair<Int, Int>?
                    if (targetObjectSizeMm != null) {
                        result = AnalysisResult(
                            sortedResults = sortedResults,
                            referenceObjectIndex = referenceObjectIndex,
                            targetObjectIndex = targetObjectIndex,
                            targetObjectSizeMillimetres = targetObjectSizeMm
                        )
                    }
                }
            }
        }

        Timber.d("Analyser.analyse() duration = ${analysisTime.inWholeMilliseconds} ms")

        return result
    }
}