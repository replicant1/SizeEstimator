package com.example.sizeestimator.domain

/**
 * @property minTopPx Minimum vertical coordinate of the reference object's "top" coordinate.
 */
class ReferenceObjectFinder(private val minTopPx: Float) : ScoreboardProcessor<ScoreboardItem> {

    /**
     * Find the reference object - it is the highest scoring result underneath [minTopPx]
     *
     * @return Element of [scoreboard] that corresponds to the reference object. null if not found.
     */
    override fun process(scoreboard: Scoreboard) : ScoreboardItem? {
        scoreboard.list.forEach { result ->
            if (result.location.top > minTopPx) {
                return result
            }
        }
        return null
    }
}