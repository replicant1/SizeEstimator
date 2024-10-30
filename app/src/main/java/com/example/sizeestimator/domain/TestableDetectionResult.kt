package com.example.sizeestimator.domain

import android.graphics.RectF
import com.example.sizeestimator.ml.SsdMobilenetV1
import kotlin.math.abs

/**
 * Needed because [SsdMobilenetV1.DetectionResult] (as generated) has private constructor.

 */
data class TestableDetectionResult(
    val score: Float,
    val location: BoundingBox,
)

/**
 * The [RectF] property of [SsdMobilenetV1.DetectionResult.getLocationAsRectF] is mocked
 * for (test) source tree.
 */
data class BoundingBox(val top: Float, val left: Float, val bottom: Float, val right: Float) {
    fun width(): Float {
        return abs(right - left)
    }

    fun height(): Float {
        return abs(top - bottom)
    }
}

/**
 * Convert between models, information is preserved.
 */
fun List<SsdMobilenetV1.DetectionResult>.toTestable(): List<TestableDetectionResult> {
    return map {
        TestableDetectionResult(
            it.scoreAsFloat,
            BoundingBox(
                top = it.locationAsRectF.top,
                left = it.locationAsRectF.left,
                bottom = it.locationAsRectF.bottom,
                right = it.locationAsRectF.right
            )
        )
    }
}

fun BoundingBox.toRectF() : RectF {
    return RectF(
        this.left,
        this.top,
        this.right,
        this.bottom,
    )
}