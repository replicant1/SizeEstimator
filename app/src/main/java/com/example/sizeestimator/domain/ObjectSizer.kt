package com.example.sizeestimator.domain

import android.util.Size
import com.example.sizeestimator.BuildConfig

class ObjectSizer(
    private val referenceObjectIndex: Int,
    private val targetObjectIndex: Int
) : SortedResultListProcessor {

    override fun process(sortedResults: SortedResultList): Pair<Int, Int>? {
        return if (
            sortedResults.hasIndex(referenceObjectIndex) &&
            sortedResults.hasIndex(targetObjectIndex)
        ) {
            val referenceObjectResult = sortedResults.sortedResultList[referenceObjectIndex]
            val targetObjectResult = sortedResults.sortedResultList[targetObjectIndex]

            val referenceObjectWidthPx = referenceObjectResult.location.width()
            val mmPerPixel = BuildConfig.REFERENCE_OBJECT_WIDTH_MM / referenceObjectWidthPx

            val actualTargetObjectWidthMm = targetObjectResult.location.width() * mmPerPixel
            val actualTargetObjectHeightMm = targetObjectResult.location.height() * mmPerPixel

            Pair(actualTargetObjectWidthMm.toInt(), actualTargetObjectHeightMm.toInt())
        } else {
            null
        }
    }
}