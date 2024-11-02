package com.example.sizeestimator.domain

import com.example.sizeestimator.BuildConfig

/**
 * @property referenceObject as found with [ReferenceObjectFinder] amongst [Scoreboard]
 * @property targetObject as found with [TargetObjectFinder] amongst [Scoreboard]
 */
class ObjectSizer(
    private val referenceObject: ScoreboardItem,
    private val targetObject: ScoreboardItem
) : ScoreboardProcessor<Pair<Int, Int>> {

    override fun process(scoreboard: Scoreboard): Pair<Int, Int>? {
        val referenceObjectWidthPx = referenceObject.location.width()
        if (referenceObjectWidthPx <= 0) {
            return null
        }

        val mmPerPixel = BuildConfig.REFERENCE_OBJECT_WIDTH_MM / referenceObjectWidthPx

        val actualTargetObjectWidthMm = targetObject.location.width() * mmPerPixel
        val actualTargetObjectHeightMm = targetObject.location.height() * mmPerPixel

        return Pair(actualTargetObjectWidthMm.toInt(), actualTargetObjectHeightMm.toInt())
    }
}