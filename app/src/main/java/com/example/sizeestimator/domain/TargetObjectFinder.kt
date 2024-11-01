package com.example.sizeestimator.domain

/**
 * @property referenceObject as previously found with [ReferenceObjectFinder]
 */
class TargetObjectFinder(private val referenceObject: TestableDetectionResult) :
    SortedResultListProcessor<TestableDetectionResult> {

    /**
     * Find the target object - it is the highest scoring result above the reference object.
     *
     * @return Index into receiving List of the target object. null if not found.
     */
    override fun process(sortedResults: SortedResultList) : TestableDetectionResult? {
        sortedResults.list.forEach { result ->
            if (result.location.bottom < referenceObject.location.top) {
                return result
            }
        }
        return null
    }
}