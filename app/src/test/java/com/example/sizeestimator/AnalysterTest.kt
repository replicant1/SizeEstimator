package com.example.sizeestimator

import android.util.Size
import com.example.sizeestimator.data.LoresBitmap
import com.example.sizeestimator.domain.Analyser
import com.example.sizeestimator.domain.BoundingBox
import com.example.sizeestimator.domain.ObjectSizer
import com.example.sizeestimator.domain.ReferenceObjectFinder
import com.example.sizeestimator.domain.SortedResultList
import com.example.sizeestimator.domain.TargetObjectFinder
import com.example.sizeestimator.domain.TestableDetectionResult
import org.junit.Test

import org.junit.Assert.*


class AnalysterTest {

    @Test
    fun `find reference object when there is exactly one`() {
        val result1 = TestableDetectionResult(
            score = 0.9F,
            location = BoundingBox(left = 100F, top = 50F, bottom = 200F, right = 170F)
        )
        val result2 = TestableDetectionResult(
            score = 0.8F,
            location = BoundingBox(left = 30F, top = 190F, bottom = 220F, right = 220F)
        )
        val sortedResults = SortedResultList(listOf(result1, result2))
        val refIndex = sortedResults.process(ReferenceObjectFinder(150F)) as Int?

        // Sorted results = [result1, result2]. result2 is the ref object as top > 150F
        assertEquals(1, refIndex)
    }

    @Test
    fun `find reference object when there is none`() {
        val result1 = TestableDetectionResult(
            score = 0.9F,
            location = BoundingBox(left = 10F, top = 10F, bottom = 100F, right = 200F)
        )
        val result2 = TestableDetectionResult(
            score = 0.8F,
            location = BoundingBox(left = 10F, top = 20F, bottom = 110F, right = 200F)
        )
        val detectedResults = SortedResultList(listOf(result1, result2))
        val refIndex = detectedResults.process(ReferenceObjectFinder(150F)) as Int?

        assertNull(refIndex)
    }

    @Test
    fun `find target object when there are no competing objects`() {
        val aboveTheMidpoint = TestableDetectionResult(
            score = 0.9F,
            location = BoundingBox(left = 10F, top = 10F, bottom = 100F, right = 200F)
        )
        val belowTheMidpoint = TestableDetectionResult(
            score = 0.8F,
            location = BoundingBox(left = 10F, top = 180F, bottom = 200F, right = 200F)
        )
        val detectedResults = SortedResultList(listOf(aboveTheMidpoint, belowTheMidpoint))
        val refIndex = detectedResults.process(ReferenceObjectFinder(150F)) as Int?

        assertEquals(1, refIndex)
    }

    @Test
    fun `find target object when there is only one candidate`() {
        val target = TestableDetectionResult(
            score = 0.9F,
            location = BoundingBox(left = 10F, top = 10F, bottom = 100F, right = 200F)
        )
        val reference = TestableDetectionResult(
            score = 0.8F,
            location = BoundingBox(left = 10F, top = 200F, bottom = 300F, right = 200F)
        )
        val detectedResults = SortedResultList(listOf(target, reference))
        val targetIndex = detectedResults.process(TargetObjectFinder(1)) as Int?

        assertEquals(0, targetIndex)
    }

    @Test
    fun `find target object when there are two candidates`() {
        val target1 = TestableDetectionResult(
            score = 0.9F,
            location = BoundingBox(left = 10F, top = 10F, bottom = 100F, right = 200F)
        )
        val target2 = TestableDetectionResult(
            score = 0.85F,
            location = BoundingBox(left = 10F, top = 15F, bottom = 110F, right = 205F)
        )
        val reference = TestableDetectionResult(
            score = 0.6F,
            location = BoundingBox(left = 12F, top = 180F, bottom = 200F, right = 210F)
        )
        val sortedResults = SortedResultList(listOf(target1, target2, reference))
        val targetIndex = sortedResults.process(TargetObjectFinder(2)) as Int?

        assertEquals(0, targetIndex)
    }

    @Test
    fun `calculate target object size when there is exactly one target object`() {
        val target = TestableDetectionResult(
            score = 0.9F,
            location = BoundingBox(left = 10F, top = 10F, bottom = 100F, right = 200F)
        )
        val reference = TestableDetectionResult(
            score = 0.8F,
            location = BoundingBox(left = 10F, top = 200F, bottom = 250F, right = 200F)
        )
        val sortedResults = SortedResultList(listOf(target, reference))
        val size = sortedResults.process(
            ObjectSizer(
                referenceObjectIndex = 1,
                targetObjectIndex = 0
            )
        ) as Pair<Int, Int>

        val refWidthPx = reference.location.width()
        val mmPerPixel = BuildConfig.REFERENCE_OBJECT_WIDTH_MM / refWidthPx
        val targetWidthMm = target.location.width() * mmPerPixel
        val targetHeightMm = target.location.height() * mmPerPixel

        assertEquals(size.first, targetWidthMm.toInt())
        assertEquals(size.second, targetHeightMm.toInt())
    }

    @Test
    fun `analyse scene with one reference object and one target object`() {
        val target = TestableDetectionResult(
            score = 0.9F,
            location = BoundingBox(left = 10F, top = 10F, bottom = 100F, right = 200F)
        )
        val reference = TestableDetectionResult(
            score = 0.8F,
            location = BoundingBox(left = 10F, top = 200F, bottom = 250F, right = 200F)
        )
        val sortedResults = SortedResultList(listOf(target, reference))
        val analyser = Analyser(sortedResults)
        val analysisResult = analyser.analyse(LoresBitmap.AnalysisOptions(minTop = 150F))

        assertEquals(target, analysisResult?.sortedResults?.sortedResultList?.get(0))
        assertEquals(reference, analysisResult?.sortedResults?.sortedResultList?.get(1))
        assertEquals(1, analysisResult?.referenceObjectIndex)
        assertEquals(0, analysisResult?.targetObjectIndex)

        val refWidthPx = reference.location.width()
        val mmPerPixel = BuildConfig.REFERENCE_OBJECT_WIDTH_MM / refWidthPx
        val targetWidthMm = target.location.width() * mmPerPixel
        val targetHeightMm = target.location.height() * mmPerPixel

        assertEquals(targetWidthMm.toInt(), analysisResult?.targetObjectSizeMillimetres?.first)
        assertEquals(targetHeightMm.toInt(), analysisResult?.targetObjectSizeMillimetres?.second)
    }

    @Test
    fun `find target object when reference object was not found`() {
        val target = TestableDetectionResult(
            score = 0.9F,
            location = BoundingBox(left = 10F, top = 10F, bottom = 100F, right = 200F)
        )
        val sortedResults = SortedResultList(listOf(target))
        val result = sortedResults.process(TargetObjectFinder(-1))

        // Target object cannot be found unless reference object is found
        assertEquals(-1, result)
    }
}