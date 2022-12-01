package com.example.photo_vocalizer.classification

import android.content.Context
import android.graphics.Bitmap
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.photo_vocalizer.R
import com.example.photo_vocalizer.ml.FruitModel
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.ByteBuffer
import java.nio.ByteOrder

class Classification (context: Context, imageSize: Int, resultText : TextView){
    private var imageSize : Int
    private var context : Context

    private var resultText : TextView
    private val classes = arrayOf("Apple", "Banana", "Orange")
    private var colors : Array<Int>

    init {
        this.imageSize = imageSize
        this.context = context
        this.resultText = resultText
        this.colors = arrayOf(ContextCompat.getColor(context, R.color.red),
            ContextCompat.getColor(context, R.color.yellow),
            ContextCompat.getColor(context, R.color.orange))
    }

    fun classifyImage(context: Context, rescaledBitmap: Bitmap) {
        try {
            val model = FruitModel.newInstance(context)
            // Creates inputs for reference.
            val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 32, 32, 3), DataType.FLOAT32)
            inputFeature0.loadBuffer(createByteBuffer(rescaledBitmap))
            // Runs model inference and gets result.
            val outputs: FruitModel.Outputs = model.process(inputFeature0)
            val outputFeature0: TensorBuffer = outputs.outputFeature0AsTensorBuffer
            val confidences = outputFeature0.floatArray
            // set the found class based on the confidences
            setClassifiedClassText(confidences)
            setClassifiedClassColor(confidences)
            // Releases model resources if no longer used.
            model.close()
        } catch (e: Exception) {
            Toast.makeText(context, "Classification failed", Toast.LENGTH_LONG).show()
        }
    }

    private fun createByteBuffer(rescaledBitmap: Bitmap) : ByteBuffer {
        // (Float[4] * imageSize ^2 * rgbValues[3])
        val byteBuffer: ByteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3)
        byteBuffer.order(ByteOrder.nativeOrder())
        val intValues = IntArray(imageSize * imageSize)
        rescaledBitmap.getPixels(intValues, 0, rescaledBitmap.width, 0, 0, rescaledBitmap.width, rescaledBitmap.height)
        var pixel = 0
        // Iterate over all the pixels, getting the RGB values for each one
        for (i in 0 until imageSize) {
            for (j in 0 until imageSize) {
                val `val` = intValues[pixel++] // RGB
                byteBuffer.putFloat((`val` shr 16 and 0xFF) * (1f / 1))
                byteBuffer.putFloat((`val` shr 8 and 0xFF) * (1f / 1))
                byteBuffer.putFloat((`val` and 0xFF) * (1f / 1))
            }
        }
        return byteBuffer
    }

    private fun setClassifiedClassText(confidences: FloatArray){
        val maxPos = getMaxPosition(confidences)
        resultText.text = classes[maxPos]
    }

    private fun setClassifiedClassColor(confidences: FloatArray){
        val maxPos = getMaxPosition(confidences)
        resultText.setTextColor(colors[maxPos])
    }

    private fun getMaxPosition(confidences: FloatArray) : Int{
        var maxPos = 0
        var maxConfidence = 0f
        for (i in confidences.indices) {
            if (confidences[i] > maxConfidence) {
                maxConfidence = confidences[i]
                maxPos = i
            }
        }
        return maxPos
    }
}


