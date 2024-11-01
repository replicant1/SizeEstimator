package com.example.sizeestimator.domain

import android.graphics.RectF
import com.example.sizeestimator.ml.SsdMobilenetV1
import kotlin.math.abs

/**
 * Needed because [SsdMobilenetV1.DetectionResult] (as generated) has private constructor
 * which inhibits testing.
 */
data class ScoreboardItem(
    val score: Float,
    val location: BoundingBox,
) {
    companion object {
        fun fromTensorFlow(tf: SsdMobilenetV1.DetectionResult): ScoreboardItem {
            return ScoreboardItem(tf.scoreAsFloat, BoundingBox.fromRectF(tf.locationAsRectF))
        }
    }
}

/**
 * The [RectF] property of [SsdMobilenetV1.DetectionResult.getLocationAsRectF] is mocked
 * for (test) source tree so this provides an alternative.
 */
data class BoundingBox(val top: Float, val left: Float, val bottom: Float, val right: Float) {
    fun width(): Float {
        return abs(right - left)
    }

    fun height(): Float {
        return abs(top - bottom)
    }

    companion object {
        fun fromRectF(rect : RectF) : BoundingBox {
            return BoundingBox(
                top = rect.top,
                left = rect.left,
                right = rect.right,
                bottom = rect.bottom
            )
        }
    }
}

/**
 * Convert between Tensor Flow models and our own [Scoreboard] model
 */
fun List<SsdMobilenetV1.DetectionResult>.toTestable(): List<ScoreboardItem> {
    return map {
        ScoreboardItem(
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