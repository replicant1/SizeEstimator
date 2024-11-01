package com.example.sizeestimator.domain

data class MeasurementTrace(
    val scoreboard: Scoreboard,
    val referenceObject: ScoreboardItem,
    val targetObject: ScoreboardItem,
    val targetObjectSizeMm: Pair<Int, Int>
)