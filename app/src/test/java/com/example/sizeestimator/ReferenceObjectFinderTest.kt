package com.example.sizeestimator

import com.example.sizeestimator.domain.scoreboard.BoundingBox
import com.example.sizeestimator.domain.scoreboard.processor.ReferenceObjectFinder
import com.example.sizeestimator.domain.scoreboard.Scoreboard
import com.example.sizeestimator.domain.scoreboard.ScoreboardItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ReferenceObjectFinderTest {
    @Test
    fun `find reference object when there is exactly one`() {
        val result1 = ScoreboardItem(
            score = 0.9F,
            location = BoundingBox(left = 100F, top = 50F, bottom = 200F, right = 170F)
        )
        val result2 = ScoreboardItem(
            score = 0.8F,
            location = BoundingBox(left = 30F, top = 190F, bottom = 220F, right = 220F)
        )
        val sortedResults = Scoreboard(listOf(result1, result2))
        val refObject = ReferenceObjectFinder(150F).process(sortedResults)

        // Sorted results = [result1, result2]. result2 is the ref object as top > 150F
        assertNotNull(refObject)
        assertEquals(1, sortedResults.list.indexOf(refObject))
    }

    @Test
    fun `find reference object when there is none`() {
        val result1 = ScoreboardItem(
            score = 0.9F,
            location = BoundingBox(left = 10F, top = 10F, bottom = 100F, right = 200F)
        )
        val result2 = ScoreboardItem(
            score = 0.8F,
            location = BoundingBox(left = 10F, top = 20F, bottom = 110F, right = 200F)
        )
        val sortedResults = Scoreboard(listOf(result1, result2))
        val refObject = ReferenceObjectFinder(150F).process(sortedResults)

        assertNull(refObject)
    }
}