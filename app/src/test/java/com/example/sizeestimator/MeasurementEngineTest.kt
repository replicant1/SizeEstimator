package com.example.sizeestimator

import com.example.sizeestimator.domain.BoundingBox
import com.example.sizeestimator.domain.MeasurementEngine
import com.example.sizeestimator.domain.Scoreboard
import com.example.sizeestimator.domain.ScoreboardItem
import org.junit.Assert.assertEquals
import org.junit.Test

class MeasurementEngineTest {
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
            scoreboard, MeasurementEngine.MeasurementOptions(minTop = 150F)
        )

        assertEquals(target, analysisResult?.scoreboard?.list?.get(0))
        assertEquals(reference, analysisResult?.scoreboard?.list?.get(1))

        val refWidthPx = reference.location.width()
        val mmPerPixel = BuildConfig.REFERENCE_OBJECT_WIDTH_MM / refWidthPx
        val targetWidthMm = target.location.width() * mmPerPixel
        val targetHeightMm = target.location.height() * mmPerPixel

        assertEquals(targetWidthMm.toInt(), analysisResult?.targetObjectSizeMm?.first)
        assertEquals(targetHeightMm.toInt(), analysisResult?.targetObjectSizeMm?.second)
    }
}