package com.example.sizeestimator.domain

import com.example.sizeestimator.domain.scoreboard.processor.ObjectSizer
import com.example.sizeestimator.domain.scoreboard.processor.ReferenceObjectFinder
import com.example.sizeestimator.domain.scoreboard.Scoreboard
import com.example.sizeestimator.domain.scoreboard.processor.TargetObjectFinder

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