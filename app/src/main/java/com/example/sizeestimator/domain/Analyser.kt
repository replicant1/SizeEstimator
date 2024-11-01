package com.example.sizeestimator.domain

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
            val referenceObject = ReferenceObjectFinder(options.minTop).process(sortedResults)
            if (referenceObject != null) {
                val targetObject = TargetObjectFinder(referenceObject).process(sortedResults)
                if (targetObject != null) {
                    val targetObjectSizeMm =
                        ObjectSizer(referenceObject, targetObject).process(sortedResults)
                    if (targetObjectSizeMm != null) {
                        result = AnalysisResult(
                            sortedResults,
                            referenceObject,
                            targetObject,
                            targetObjectSizeMm
                        )
                    }
                }
            }
        }

        Timber.d("Analyser.analyse() duration = ${analysisTime.inWholeMilliseconds} ms")

        return result
    }
}