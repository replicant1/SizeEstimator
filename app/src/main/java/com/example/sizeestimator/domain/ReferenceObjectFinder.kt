package com.example.sizeestimator.domain

/**
 * @property minTop Minimum vertical coordinate of the reference object,
 */
class ReferenceObjectFinder(private val minTop: Float) :
    SortedResultListProcessor<TestableDetectionResult> {

    /**
     * Find the reference object - it is the highest scoring result underneath [minTop]
     *
     * @return Element of [sortedResults] that corresponds to the reference object. null if not found.
     */
    override fun process(sortedResults: SortedResultList) : TestableDetectionResult? {
        sortedResults.list.forEach { result ->
            if (result.location.top > minTop) {
                return result
            }
        }
        return null
    }
}