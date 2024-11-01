package com.example.sizeestimator.domain

interface ScoreboardProcessor<T> {

    /**
     * @return result of processing the [scoreboard] or null if processing
     * was not possible.
     */
    fun process(scoreboard : Scoreboard) : T?
}