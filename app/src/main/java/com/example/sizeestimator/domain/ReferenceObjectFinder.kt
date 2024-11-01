package com.example.sizeestimator.domain

/**
 * @property minTop Minimum vertical coordinate of the reference object,
 */
class ReferenceObjectFinder(private val minTop: Float) :
    ScoreboardProcessor<ScoreboardItem> {

    /**
     * Find the reference object - it is the highest scoring result underneath [minTop]
     *
     * @return Element of [scoreboard] that corresponds to the reference object. null if not found.
     */
    override fun process(scoreboard: Scoreboard) : ScoreboardItem? {
        scoreboard.list.forEach { result ->
            if (result.location.top > minTop) {
                return result
            }
        }
        return null
    }
}