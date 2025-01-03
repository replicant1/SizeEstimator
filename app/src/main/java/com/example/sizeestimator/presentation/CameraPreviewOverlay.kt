package com.example.sizeestimator.presentation

import androidx.compose.runtime.State
import androidx.compose.ui.draw.CacheDrawScope
import androidx.compose.ui.draw.DrawResult
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.sizeestimator.domain.bitmap.LoresBitmap
import com.example.sizeestimator.domain.scoreboard.BoundingBox

import com.example.sizeestimator.domain.MeasurementTrace

/**
 * Draws an overlay on top of the camera preview that shows the target and reference bounding
 * boxes as per [trace].
 */
fun drawOverlay(trace: State<MeasurementTrace?>): CacheDrawScope.() -> DrawResult {
    return {
        // Values in this section are only recalculated when the state variables they depend upon
        // change value. The drawing commands in the "onDrawContent" lambda are redrawn with every blit.

        // Find reference object's bounding box
        val referenceBox: BoundingBox? = trace.value?.referenceObject?.location

        // Find target object's bounding box
        val targetBox: BoundingBox? = trace.value?.targetObject?.location

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
        val referenceBoxColor = Color.Yellow
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

        var referenceBoxTopLeft: Offset? = null
        var referenceBoxSize: Size? = null

        if (referenceBox != null) {
            referenceBoxTopLeft = Offset(
                boxXLeftOffset + referenceBox.left * scale,
                referenceBox.top * scale
            )
            referenceBoxSize = Size(
                referenceBox.width() * scale,
                referenceBox.height() * scale
            )
        }

        var targetBoxTopLeft: Offset? = null
        var targetBoxSize: Size? = null

        if (targetBox != null) {
            targetBoxTopLeft = Offset(
                boxXLeftOffset + targetBox.left * scale,
                targetBox.top * scale
            )
            targetBoxSize = Size(
                targetBox.width() * scale,
                targetBox.height() * scale
            )
        }

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
            if ((referenceBoxTopLeft != null) && (referenceBoxSize != null)) {
                drawRect(
                    color = referenceBoxColor,
                    topLeft = referenceBoxTopLeft,
                    size = referenceBoxSize,
                    style = Stroke(width = boxStrokeWidth)
                )
            }

            // Bounding box of the target object as per analysis
            if ((targetBoxTopLeft != null) && (targetBoxSize != null)) {
                drawRect(
                    color = targetBoxColor,
                    topLeft = targetBoxTopLeft,
                    size = targetBoxSize,
                    style = Stroke(width = boxStrokeWidth)
                )
            }
        }
    }
}