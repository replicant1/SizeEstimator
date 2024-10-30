package com.example.sizeestimator.domain

import androidx.annotation.VisibleForTesting
import com.example.sizeestimator.BuildConfig
import com.example.sizeestimator.data.LoresBitmap
import timber.log.Timber

/**
 * Analyses the output of the Tensor Flow model and deduces an estimate for the
 * actual size of the target object.
 * @param results output of the Tensor Flow model - bounding boxes with scores
 */
class Analyser(private val results: List<TestableDetectionResult>) {
    private val sortedResults = results.sortedByDescending { it.score }

    /**
     * Analyse the Tensor Flow results provided to constructor.
     */
    fun analyse(options: LoresBitmap.AnalysisOptions): AnalysisResult {
        val analysisStartTime = System.currentTimeMillis()
        val referenceObjectIndex = findReferenceObject(options.minTop)
        val targetObjectIndex = findTargetObject(referenceObjectIndex)
        val targetObjectSizeMillimetres =
            calculateTargetObjectSize(referenceObjectIndex, targetObjectIndex)
        val analysisEndTime = System.currentTimeMillis()

        Timber.d("Analyser.analyse() time = ${analysisEndTime - analysisStartTime} milliseconds")

        return AnalysisResult(
            sortedResults = sortedResults,
            referenceObjectIndex = referenceObjectIndex,
            targetObjectIndex = targetObjectIndex,
            targetObjectSizeMillimetres = targetObjectSizeMillimetres
        )
    }

    /**
     * Find the reference object - it is the highest scoring result underneath the vertical midpoint
     * of the image. The receiving list must be sorted from highest score to lowest score.
     *
     * @param minTop Minimum vertical coordinate of the reference object,
     * @return Index into receiving List of the reference object. -1 if not found.
     */
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun findReferenceObject(minTop: Float): Int {
        sortedResults.forEachIndexed { index, result ->
            if (result.location.top > minTop) {
                return index
            }
        }
        return UNKNOWN
    }

    /**
     * Find the target object - it is the highest scoring result above the reference object.
     *
     * @param referenceObjectIndex Index of element in receiver corresponding to reference object
     * @return Index into receiving List of the target object. -1 if not found.
     */
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun findTargetObject(referenceObjectIndex: Int): Int {
        if (referenceObjectIndex < 0 || referenceObjectIndex >= sortedResults.size) {
            return UNKNOWN
        }
        val referenceObject = sortedResults[referenceObjectIndex]
        sortedResults.forEachIndexed { index, result ->
            if (result.location.bottom < referenceObject.location.top) {
                return index
            }
        }
        return UNKNOWN
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun calculateTargetObjectSize(
        referenceObjectIndex: Int,
        targetObjectIndex: Int
    ): Pair<Long, Long> {

        if ((referenceObjectIndex == UNKNOWN) || (targetObjectIndex == UNKNOWN)) {
            return Pair(UNKNOWN.toLong(), UNKNOWN.toLong())
        }

        val referenceObjectResult = sortedResults[referenceObjectIndex]
        val targetObjectResult = sortedResults[targetObjectIndex]

        val referenceObjectWidthPx = referenceObjectResult.location.width()
        val mmPerPixel = BuildConfig.REFERENCE_OBJECT_WIDTH_MM / referenceObjectWidthPx

        val actualTargetObjectWidthMm = targetObjectResult.location.width() * mmPerPixel
        val actualTargetObjectHeightMm = targetObjectResult.location.height() * mmPerPixel

        return Pair(actualTargetObjectWidthMm.toLong(), actualTargetObjectHeightMm.toLong())
    }

    companion object {
        const val UNKNOWN = -1
    }

}