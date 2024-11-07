package com.example.sizeestimator.domain

import com.example.sizeestimator.domain.scoreboard.Scoreboard
import com.example.sizeestimator.domain.scoreboard.ScoreboardItem

/**
 * Overall result of attempt to estimate the size of the target object, and the intermediate
 * results for the purpose of tracking the algorithm's working.
 */
data class MeasurementTrace(
    val scoreboard: Scoreboard,
    val referenceObject: ScoreboardItem,
    val targetObject: ScoreboardItem,
    val targetObjectSizeMm: Pair<Int, Int> // width, height in millimetres
)