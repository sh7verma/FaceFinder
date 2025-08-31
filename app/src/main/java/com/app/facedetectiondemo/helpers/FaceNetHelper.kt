package com.app.facedetectiondemo.helpers

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp

class FaceNetHelper(context: Context) {
    private val interpreter: Interpreter
    private val inputSize = 160
    private val embeddingDim = 512
    private val imageProcessor = ImageProcessor.Builder()
        .add(ResizeOp(inputSize, inputSize, ResizeOp.ResizeMethod.BILINEAR))
        .add(NormalizeOp(127.5f, 127.5f))
        .build()

    init {
        val model = FileUtil.loadMappedFile(context, "facenet_512.tflite")

        // Configure interpreter options
        val options = Interpreter.Options().apply {
            setNumThreads(4)      // Use multiple threads for better performance
            setUseNNAPI(true)      // Enable NNAPI for hardware acceleration if available
        }

        interpreter = Interpreter(model, options)
    }

    fun getEmbedding(bitmap: Bitmap): FloatArray {
        val resized = Bitmap.createScaledBitmap(bitmap, inputSize, inputSize, true)
        val tensorImage = TensorImage(org.tensorflow.lite.DataType.FLOAT32)
        tensorImage.load(resized)
        val processedImage = imageProcessor.process(tensorImage)

        val output = Array(1) { FloatArray(embeddingDim) }
        interpreter.run(processedImage.buffer, output)
        return output[0]
    }

    fun close() {
        interpreter.close() // Free native resources when done
    }
}
