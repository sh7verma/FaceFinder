package com.app.facedetectiondemo.helpers

import android.graphics.Bitmap
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions

class FaceDetectorHelper {

    private val detector: FaceDetector

    init {
        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
            .enableTracking()
            .build()

        detector = FaceDetection.getClient(options)
        Log.d("FaceDetectorHelper", "FaceDetector initialized")
    }

    fun detectFaces(
        bitmap: Bitmap,
        onResult: (faces: List<Face>) -> Unit,
        onError: (Exception) -> Unit = { Log.e("FaceDetectorHelper", "Face detection failed", it) }
    ) {
        Log.d("FaceDetectorHelper", "detectFaces called")
        val image = InputImage.fromBitmap(bitmap, 0)
        detector.process(image)
            .addOnSuccessListener { faces ->
                Log.d("FaceDetectorHelper", "Detection success, faces found: ${faces.size}")
                onResult(faces)
            }
            .addOnFailureListener { e ->
                Log.e("FaceDetectorHelper", "Detection failed", e)
                onError(e)
            }
    }

    fun close() {
        Log.d("FaceDetectorHelper", "Closing detector")
        detector.close()
    }

    fun cropFace(bitmap: Bitmap, rect: android.graphics.Rect): Bitmap {
        Log.d("FaceViewModel", "cropFace called with rect: $rect")
        val safeRect = android.graphics.Rect(
            rect.left.coerceAtLeast(0),
            rect.top.coerceAtLeast(0),
            rect.right.coerceAtMost(bitmap.width),
            rect.bottom.coerceAtMost(bitmap.height)
        )
        Log.d("FaceViewModel", "Safe rect: $safeRect")
        return Bitmap.createBitmap(
            bitmap,
            safeRect.left,
            safeRect.top,
            safeRect.width(),
            safeRect.height()
        )
    }

}