package com.example.sizeestimator.domain

class TargetObjectFinder(private val referenceObjectIndex: Int) : SortedResultListProcessor {

    /**
     * Find the target object - it is the highest scoring result above the reference object.
     *
     * @param referenceObjectIndex Index of element in receiver corresponding to reference object
     * @return Index into receiving List of the target object. -1 if not found.
     */
    override fun process(sortedResults: SortedResultList) : Int? {
        if (referenceObjectIndex < 0 || referenceObjectIndex >= sortedResults.sortedResultList.size) {
            return SortedResultList.UNKNOWN
        }

        val referenceObject = sortedResults.sortedResultList[referenceObjectIndex]

        sortedResults.sortedResultList.forEachIndexed { index, result ->
            if (result.location.bottom < referenceObject.location.top) {
                return index
            }
        }
        return SortedResultList.UNKNOWN
    }
}