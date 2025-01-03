package com.example.sizeestimator.domain.scoreboard

/**
 * Convenience so sorting occurs only once and then is passed around immutably.
 */
class Scoreboard(results: List<ScoreboardItem>) {
    val list = results.sortedByDescending { it.score }
}