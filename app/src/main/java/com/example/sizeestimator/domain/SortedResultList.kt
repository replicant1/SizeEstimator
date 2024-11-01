package com.example.sizeestimator.domain

/**
 * Convenience so sorting occurs only once and then is passed around immutably.
 */
class SortedResultList(results: List<TestableDetectionResult>) {
    val list = results.sortedByDescending { it.score }
}