package com.example.sizeestimator

import com.example.sizeestimator.data.LoresBitmap
import com.example.sizeestimator.domain.Analyser
import com.example.sizeestimator.domain.BoundingBox
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
        val detectedResults = listOf(result1, result2)
        val analyser = Analyser(detectedResults)

        val refIndex = analyser.findReferenceObject(150F)

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
        val detectedResults = listOf(result1, result2)
        val analyser = Analyser(detectedResults)
        val refIndex = analyser.findReferenceObject(150F)

        assertEquals(-1, refIndex)
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
        val detectedResults = listOf(aboveTheMidpoint, belowTheMidpoint)
        val analyser = Analyser(detectedResults)
        val refIndex = analyser.findTargetObject(1)

        assertEquals(0, refIndex)
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
        val detectedResults = listOf(target, reference)
        val analyser = Analyser(detectedResults)
        val targetIndex = analyser.findTargetObject(1)

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
        val unsortedResults = listOf(target1, target2, reference)
        val analyser = Analyser(unsortedResults)
        val targetIndex = analyser.findTargetObject(2)

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
        val unsortedResults = listOf(target, reference)
        val analyser = Analyser(unsortedResults)
        val size = analyser.calculateTargetObjectSize(
            referenceObjectIndex = 1,
            targetObjectIndex = 0
        )

        val refWidthPx = reference.location.width()
        val mmPerPixel = BuildConfig.REFERENCE_OBJECT_WIDTH_MM / refWidthPx
        val targetWidthMm = target.location.width() * mmPerPixel
        val targetHeightMm = target.location.height() * mmPerPixel

        assertEquals(size.first, targetWidthMm.toLong())
        assertEquals(size.second, targetHeightMm.toLong())
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
        val unsortedResults = listOf(target, reference)
        val analyser = Analyser(unsortedResults)
        val analysisResult = analyser.analyse(LoresBitmap.AnalysisOptions(minTop =  150F))

        assertEquals(target, analysisResult.sortedResults[0])
        assertEquals(reference, analysisResult.sortedResults[1])
        assertEquals(1, analysisResult.referenceObjectIndex)
        assertEquals(0, analysisResult.targetObjectIndex)

        val refWidthPx = reference.location.width()
        val mmPerPixel = BuildConfig.REFERENCE_OBJECT_WIDTH_MM / refWidthPx
        val targetWidthMm = target.location.width() * mmPerPixel
        val targetHeightMm = target.location.height() * mmPerPixel

        assertEquals(targetWidthMm.toLong(), analysisResult.targetObjectSizeMillimetres.first)
        assertEquals(targetHeightMm.toLong(), analysisResult.targetObjectSizeMillimetres.second)
    }
}