package com.example.sizeestimator.domain

/**
 * Convenience so sorting occurs only once.
 */
class SortedResultList(results: List<TestableDetectionResult>) {
    val list = results.sortedByDescending { it.score }
}