package com.example.sizeestimator.domain

class SortedResultList(results: List<TestableDetectionResult>) {
    val sortedResultList = results.sortedByDescending { it.score }

    fun hasIndex(index : Int) : Boolean {
        return (index >= 0) && (index < sortedResultList.size)
    }

    fun process(processor: SortedResultListProcessor) : Any? {
        return processor.process(this)
    }

    companion object {
        const val UNKNOWN = -1
    }
}