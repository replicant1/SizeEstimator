package com.example.sizeestimator.domain

data class MeasurementTrace(
    val sortedResults: Scoreboard,
    val referenceObject: ScoreboardItem,
    val targetObject: ScoreboardItem,
    val targetObjectSizeMm: Pair<Int, Int>
)