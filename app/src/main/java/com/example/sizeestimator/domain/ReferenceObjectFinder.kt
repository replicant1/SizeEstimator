package com.example.sizeestimator.domain

class ReferenceObjectFinder(private val minTop: Float) : SortedResultListProcessor {

    /**
     * Find the reference object - it is the highest scoring result underneath the vertical midpoint
     * of the image. The receiving list must be sorted from highest score to lowest score.
     *
     * @param minTop Minimum vertical coordinate of the reference object,
     * @return Index into receiving List of the reference object. -1 if not found.
     */
    override fun process(sortedResults: SortedResultList) : Int? {
        sortedResults.sortedResultList.forEachIndexed { index, result ->
            if (result.location.top > minTop) {
                return index
            }
        }
        return null
    }
}