package com.example.sizeestimator.domain

import com.example.sizeestimator.data.LoresBitmap
import timber.log.Timber
import kotlin.time.measureTime

/**
 * @param sortedResults output of the Tensor Flow model - bounding boxes with scores
 */
class Analyser(private val sortedResults: SortedResultList) {

    /**
     * @return result of finding reference and target objects from amongst [sortedResults].
     * null if analysis not possible.
     */
    fun analyse(options: LoresBitmap.AnalysisOptions): AnalysisResult? {
        var result: AnalysisResult? = null

        val analysisTime = measureTime {
            val referenceObject = ReferenceObjectFinder(options.minTop).process(sortedResults)
            referenceObject?.run {
                val targetObject = TargetObjectFinder(referenceObject).process(sortedResults)
                targetObject?.run {
                    result = AnalysisResult(
                        sortedResults,
                        referenceObject,
                        targetObject,
                        ObjectSizer(referenceObject, targetObject).process(sortedResults)
                    )
                }
            }
        }

        Timber.d("Analyser.analyse() duration = ${analysisTime.inWholeMilliseconds} ms")

        return result
    }
}