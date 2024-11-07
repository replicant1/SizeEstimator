package com.example.sizeestimator.domain.scoreboard.processor

import com.example.sizeestimator.domain.scoreboard.Scoreboard
import com.example.sizeestimator.domain.scoreboard.ScoreboardItem

/**
 * @property referenceObject as previously found with [ReferenceObjectFinder]
 */
class TargetObjectFinder(private val referenceObject: ScoreboardItem) :
    ScoreboardProcessor<ScoreboardItem> {

    /**
     * Find the target object - it is the highest scoring result above the reference object.
     *
     * @return Index into receiving List of the target object. null if not found.
     */
    override fun process(scoreboard: Scoreboard) : ScoreboardItem? {
        scoreboard.list.forEach { item ->
            if (item.location.bottom < referenceObject.location.top) {
                return item
            }
        }
        return null
    }
}