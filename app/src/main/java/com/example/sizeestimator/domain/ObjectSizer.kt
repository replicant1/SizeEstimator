package com.example.sizeestimator.domain

import com.example.sizeestimator.BuildConfig

/**
 * @property referenceObject as found with [ReferenceObjectFinder]
 * @property targetObject as found with [TargetObjectFinder]
 */
class ObjectSizer(
    private val referenceObject: TestableDetectionResult,
    private val targetObject: TestableDetectionResult
) : SortedResultListProcessor<Pair<Int, Int>> {

    override fun process(sortedResults: SortedResultList): Pair<Int, Int>? {
        val referenceObjectWidthPx = referenceObject.location.width()
        val mmPerPixel = BuildConfig.REFERENCE_OBJECT_WIDTH_MM / referenceObjectWidthPx

        val actualTargetObjectWidthMm = targetObject.location.width() * mmPerPixel
        val actualTargetObjectHeightMm = targetObject.location.height() * mmPerPixel

        return Pair(actualTargetObjectWidthMm.toInt(), actualTargetObjectHeightMm.toInt())
    }
}