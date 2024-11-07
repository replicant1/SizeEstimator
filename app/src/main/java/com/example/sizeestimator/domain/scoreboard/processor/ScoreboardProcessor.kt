package com.example.sizeestimator.domain.scoreboard.processor

import com.example.sizeestimator.domain.scoreboard.Scoreboard

interface ScoreboardProcessor<T> {

    /**
     * @return result of processing the [scoreboard] or null if processing was not possible.
     */
    fun process(scoreboard : Scoreboard) : T?
}