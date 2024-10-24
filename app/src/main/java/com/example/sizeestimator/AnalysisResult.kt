package com.example.sizeestimator

import com.example.sizeestimator.ml.SsdMobilenetV1


data class AnalysisResult(
    val outputs: SsdMobilenetV1.Outputs,
    val referenceObjectIndex: Int,
    val targetObjectIndex: Int,
    val targetObjectSizeMillimetres: Pair<Long, Long>
)