package com.example.sizeestimator

import com.example.sizeestimator.domain.MeasurementEngine
import com.example.sizeestimator.domain.BoundingBox
import com.example.sizeestimator.domain.ObjectSizer
import com.example.sizeestimator.domain.ReferenceObjectFinder
import com.example.sizeestimator.domain.Scoreboard
import com.example.sizeestimator.domain.TargetObjectFinder
import com.example.sizeestimator.domain.ScoreboardItem
import org.junit.Test

import org.junit.Assert.*


class AnalysterTest {

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

    @Test
    fun `find target object when there are no competing objects`() {
        val aboveTheMidpoint = ScoreboardItem(
            score = 0.9F,
            location = BoundingBox(left = 10F, top = 10F, bottom = 100F, right = 200F)
        )
        val belowTheMidpoint = ScoreboardItem(
            score = 0.8F,
            location = BoundingBox(left = 10F, top = 180F, bottom = 200F, right = 200F)
        )
        val sortedResults = Scoreboard(listOf(aboveTheMidpoint, belowTheMidpoint))
        val refObject = ReferenceObjectFinder(150F).process(sortedResults)
        assertNotNull(refObject)
        val targetObject = TargetObjectFinder(refObject!!).process(sortedResults)

        assertNotNull(targetObject)
        assertEquals(0, sortedResults.list.indexOf(aboveTheMidpoint))
    }

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

        assertEquals(target1, targetObject)
    }

    @Test
    fun `calculate target object size when there is exactly one target object`() {
        val target = ScoreboardItem(
            score = 0.9F,
            location = BoundingBox(left = 10F, top = 10F, bottom = 100F, right = 200F)
        )
        val reference = ScoreboardItem(
            score = 0.8F,
            location = BoundingBox(left = 10F, top = 200F, bottom = 250F, right = 200F)
        )
        val sortedResults = Scoreboard(listOf(target, reference))
        val size = ObjectSizer(reference, target).process(sortedResults)

        val refWidthPx = reference.location.width()
        val mmPerPixel = BuildConfig.REFERENCE_OBJECT_WIDTH_MM / refWidthPx
        val targetWidthMm = target.location.width() * mmPerPixel
        val targetHeightMm = target.location.height() * mmPerPixel

        assertEquals(size.first, targetWidthMm.toInt())
        assertEquals(size.second, targetHeightMm.toInt())
    }

    @Test
    fun `analyse scene with one reference object and one target object`() {
        val target = ScoreboardItem(
            score = 0.9F,
            location = BoundingBox(left = 10F, top = 10F, bottom = 100F, right = 200F)
        )
        val reference = ScoreboardItem(
            score = 0.8F,
            location = BoundingBox(left = 10F, top = 200F, bottom = 250F, right = 200F)
        )
        val scoreboard = Scoreboard(listOf(target, reference))
        val analysisResult = MeasurementEngine.measure(
            scoreboard, MeasurementEngine.MeasurementOptions(minTop = 150F))

        assertEquals(target, analysisResult?.sortedResults?.list?.get(0))
        assertEquals(reference, analysisResult?.sortedResults?.list?.get(1))

        val refWidthPx = reference.location.width()
        val mmPerPixel = BuildConfig.REFERENCE_OBJECT_WIDTH_MM / refWidthPx
        val targetWidthMm = target.location.width() * mmPerPixel
        val targetHeightMm = target.location.height() * mmPerPixel

        assertEquals(targetWidthMm.toInt(), analysisResult?.targetObjectSizeMm?.first)
        assertEquals(targetHeightMm.toInt(), analysisResult?.targetObjectSizeMm?.second)
    }
}