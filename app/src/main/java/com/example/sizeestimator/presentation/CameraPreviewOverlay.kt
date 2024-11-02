package com.example.sizeestimator.presentation

import androidx.compose.runtime.State
import androidx.compose.ui.draw.CacheDrawScope
import androidx.compose.ui.draw.DrawResult
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.sizeestimator.data.LoresBitmap

import com.example.sizeestimator.domain.AnalysisResult
import com.example.sizeestimator.domain.BoundingBox

/**
 * Draws an overlay on top of the camera preview that shows the target and reference bounding
 * boxes as per [analysisResultState].
 */
fun drawOverlay(analysisResultState : State<AnalysisResult?>) :  CacheDrawScope.() ->  DrawResult {
    return {
        // Values in this section are only recalculated when the state variables they depend upon
        // change value. The drawing commands in the "onDrawContent" lambda are redrawn with every blit.
        val sortedResults = analysisResultState.value?.sortedResults

        // Find reference object's bounding box
        val referenceObjectIndex = analysisResultState.value?.referenceObjectIndex
        var refBoundingBox: BoundingBox? = null
        if ((sortedResults != null) && (referenceObjectIndex != null) && (referenceObjectIndex != -1)) {
            val referenceObject = sortedResults[referenceObjectIndex]
            refBoundingBox = referenceObject.location
        }
        val referenceBox = refBoundingBox ?: BoundingBox(10f, 10f, 20f, 20f)

        // Find target object's bounding box
        val targetObjectIndex = analysisResultState.value?.targetObjectIndex
        var targBoundingBox: BoundingBox? = null
        if ((sortedResults != null) && (targetObjectIndex != null) && (targetObjectIndex != -1)) {
            val targetObject = sortedResults[targetObjectIndex]
            targBoundingBox = targetObject.location
        }
        val targetBox = targBoundingBox ?: BoundingBox(10f, 10f, 20f, 20f)

        val centerX = size.width / 2
        val centerY = size.height / 2

        // Cross-hair at center
        val crossHairColor = Color.Blue
        val crossHairSize = 50f
        val crossHairLeft = Offset(centerX - crossHairSize, centerY)
        val crossHairRight = Offset(centerX + crossHairSize, centerY)
        val crossHairTop = Offset(centerX, centerY - crossHairSize)
        val crossHairBottom = Offset(centerX, centerY + crossHairSize)

        // Bounding boxes for reference and target objects
        val boxXLeftOffset = (size.width - size.height) / 2
        val boxStrokeWidth = 4f
        val referenceBoxColor = Color.Red
        val targetBoxColor = Color.Green

        // Lores square viewport in center
        val loresSquareTopLeft = Offset(
            centerX - (size.height / 2),
            centerY - (size.height / 2)
        )
        val loresSquareSize = Size(size.height, size.height)
        val loresSquareStyle = Stroke(width = boxStrokeWidth)

        // Scaling factor (assume landscape) - from lores to preview
        val scale = size.height / LoresBitmap.LORES_IMAGE_SIZE_PX.toFloat()

        val referenceBoxTopLeft = Offset(
            boxXLeftOffset + referenceBox.left * scale,
            referenceBox.top * scale
        )
        val referenceBoxSize = Size(
            referenceBox.width() * scale,
            referenceBox.height() * scale
        )
        val targetBoxTopLeft = Offset(
            boxXLeftOffset + targetBox.left * scale,
            targetBox.top * scale
        )
        val targetBoxSize = Size(
            targetBox.width() * scale,
            targetBox.height() * scale
        )

        onDrawWithContent {
            drawContent()

            // Horizontal stroke of the cross-hair
            drawLine(
                crossHairColor,
                strokeWidth = boxStrokeWidth,
                start = crossHairLeft,
                end = crossHairRight
            )
            // Vertical stroke of the cross-hair
            drawLine(
                crossHairColor,
                strokeWidth = boxStrokeWidth,
                start = crossHairTop,
                end = crossHairBottom
            )

            // Square that anticipates the lores bitmap that
            // will ultimately be analysed by the Tensor Flow model.
            // Reference and target objects should be positioned within this.
            drawRect(
                color = crossHairColor,
                topLeft = loresSquareTopLeft,
                size = loresSquareSize,
                style = loresSquareStyle
            )

            // Bounding box of the reference object as per analysis
            drawRect(
                color = referenceBoxColor,
                topLeft = referenceBoxTopLeft,
                size = referenceBoxSize,
                style = Stroke(width = boxStrokeWidth)
            )

            // Bounding box of the target object as per analysis
            drawRect(
                color = targetBoxColor,
                topLeft = targetBoxTopLeft,
                size = targetBoxSize,
                style = Stroke(width = boxStrokeWidth)
            )
        }
    }
}