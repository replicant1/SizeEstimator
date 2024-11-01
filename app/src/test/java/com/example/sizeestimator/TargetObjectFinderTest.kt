package com.example.sizeestimator

import com.example.sizeestimator.domain.BoundingBox
import com.example.sizeestimator.domain.ObjectSizer
import com.example.sizeestimator.domain.ReferenceObjectFinder
import com.example.sizeestimator.domain.Scoreboard
import com.example.sizeestimator.domain.ScoreboardItem
import com.example.sizeestimator.domain.TargetObjectFinder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test


class TargetObjectFinderTest {

    @Test
    fun `find target object when there is only one candidate`() {
        val target = ScoreboardItem(
            score = 0.9F,
            location = BoundingBox(left = 10F, top = 10F, bottom = 100F, right = 200F)
        )
        val reference = ScoreboardItem(
            score = 0.8F,
            location = BoundingBox(left = 10F, top = 200F, bottom = 300F, right = 200F)
        )
        val sortedResults = Scoreboard(listOf(target, reference))
        val targetObject = TargetObjectFinder(reference).process(sortedResults)

        assertEquals(targetObject, target)
    }

    @Test
    fun `find target object when there are two candidates`() {
        val target1 = ScoreboardItem(
            score = 0.9F,
            location = BoundingBox(left = 10F, top = 10F, bottom = 100F, right = 200F)
        )
        val target2 = ScoreboardItem(
            score = 0.85F,
            location = BoundingBox(left = 10F, top = 15F, bottom = 110F, right = 205F)
        )
        val reference = ScoreboardItem(
            score = 0.6F,
            location = BoundingBox(left = 12F, top = 180F, bottom = 200F, right = 210F)
        )
        val sortedResults = Scoreboard(listOf(target1, target2, reference))
        val targetObject = TargetObjectFinder(reference).process(sortedResults)

        // target1 should be found because it scored highest
        assertEquals(target1, targetObject)
    }

}