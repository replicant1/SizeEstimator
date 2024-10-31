package com.example.sizeestimator.domain

import androidx.core.view.accessibility.AccessibilityViewCommand.SetTextArguments

interface SortedResultListProcessor {
    fun process(sortedResults : SortedResultList) : Any?

}