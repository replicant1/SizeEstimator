package com.example.sizeestimator.domain

/**
 * @param scoreboard output of the Tensor Flow model - bounding boxes with scores
 */
class MeasurementEngine {

    data class MeasurementOptions(val minTop: Float)

    companion object {
        fun measure(scoreboard: Scoreboard, options: MeasurementOptions): MeasurementTrace? {
            val referenceObject = ReferenceObjectFinder(options.minTop).process(scoreboard)
            referenceObject?.run {
                val targetObject = TargetObjectFinder(referenceObject).process(scoreboard)
                targetObject?.run {
                    val targetSize = ObjectSizer(referenceObject, targetObject).process(scoreboard)
                    targetSize?.run {
                        return MeasurementTrace(
                            scoreboard,
                            referenceObject,
                            targetObject,
                            targetSize
                        )
                    }
                }
            }
            return null
        }
    }
}