package com.example.sizeestimator.domain

interface SortedResultListProcessor<T> {

    /**
     * @return result of processing the [sortedResults] or null if processing
     * was not possible.
     */
    fun process(sortedResults : SortedResultList) : T?
}

