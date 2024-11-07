package com.example.sizeestimator.domain.scoreboard.processor

import com.example.sizeestimator.BuildConfig
import com.example.sizeestimator.domain.scoreboard.Scoreboard
import com.example.sizeestimator.domain.scoreboard.ScoreboardItem

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