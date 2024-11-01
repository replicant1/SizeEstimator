package com.example.sizeestimator

import com.example.sizeestimator.domain.BoundingBox
import com.example.sizeestimator.domain.ObjectSizer
import com.example.sizeestimator.domain.Scoreboard
import com.example.sizeestimator.domain.ScoreboardItem
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import org.junit.Test


class ObjectSizerTest {
    @Test
    fun `calculate target object size when one reference and one target object`() {
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

        assertEquals(size?.first, targetWidthMm.toInt())
        assertEquals(size?.second, targetHeightMm.toInt())
    }

    @Test
    fun `calculate target object size when reference object has zero width`() {
        val target = ScoreboardItem(
            score = 0.9F,
            location = BoundingBox(left = 10F, top = 10F, bottom = 100F, right = 300F)
        )
        val reference = ScoreboardItem(
            score = 0.8F,
            location = BoundingBox(left = 10F, top = 200F, bottom = 250F, right = 10F)
        )
        val scoreboard = Scoreboard(listOf(target, reference))
        val size = ObjectSizer(reference, target).process(scoreboard)
        assertNull(size)
    }
}