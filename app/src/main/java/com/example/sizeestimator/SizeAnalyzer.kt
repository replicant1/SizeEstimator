package com.example.sizeestimator

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import java.nio.ByteBuffer

class SizeAnalyzer(/*private val listener: LumaListener*/) : ImageAnalysis.Analyzer {

    private fun ByteBuffer.toByteArray() : ByteArray {
        rewind()
        val data = ByteArray(remaining())
        get(data)
        return data
    }

    override fun analyze(image: ImageProxy) {
        println("** Into analyze")
        println("** image.height = ${image.height}, image.width = ${image.width}")

        val buffer = image.planes[0].buffer
        val data = buffer.toByteArray()
        val pixels = data.map { it.toInt() and 0xFF }
        val luma = pixels.average()

        println("** luma = $luma")

        //listener(luma)

        println("** back from calling listener **")

        image.close()
    }
}