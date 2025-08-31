package com.app.facedetectiondemo.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.util.Log
import androidx.camera.core.ImageProxy
import java.io.ByteArrayOutputStream

class YuvToRgbConverter(private val context: Context) {
    fun yuvToRgb(image: ImageProxy, output: Bitmap) {
        Log.d("YuvConverter", "Starting conversion: ${image.width}x${image.height}")

        try {
            // Get the YUV data
            val planes = image.planes

            // Check if we have valid planes
            if (planes.size < 3) {
                Log.e("YuvConverter", "Not enough planes: ${planes.size}")
                throw IllegalArgumentException("Invalid image format")
            }

            val yBuffer = planes[0].buffer
            val uBuffer = planes[1].buffer
            val vBuffer = planes[2].buffer

            val ySize = yBuffer.remaining()
            val uSize = uBuffer.remaining()
            val vSize = vBuffer.remaining()

            Log.d("YuvConverter", "Buffer sizes: Y=$ySize, U=$uSize, V=$vSize")

            // More direct approach with ByteArrayOutputStream
            val out = ByteArrayOutputStream()
            val nv21 = ByteArray(image.width * image.height * 3 / 2) // Allocate sufficient space for YUV data

            // Copy Y plane
            yBuffer.get(nv21, 0, Math.min(ySize, nv21.size))

            // Copy U/V using a safer approach with bounds checking
            var uvPos = image.width * image.height

            // UV planes might have different pixel strides
            val vRowStride = planes[2].rowStride
            val vPixelStride = planes[2].pixelStride
            val uRowStride = planes[1].rowStride
            val uPixelStride = planes[1].pixelStride

            // Copy UV planes with bounds checking
            val chromaWidth = image.width / 2
            val chromaHeight = image.height / 2

            // We'll use a more careful approach - only reading available data
            for (row in 0 until chromaHeight) {
                for (col in 0 until chromaWidth) {
                    val vPos = row * vRowStride + col * vPixelStride
                    val uPos = row * uRowStride + col * uPixelStride

                    if (vPos < vBuffer.limit()) {
                        nv21[uvPos++] = vBuffer.get(vPos)
                    }

                    if (uPos < uBuffer.limit() && uvPos < nv21.size) {
                        nv21[uvPos++] = uBuffer.get(uPos)
                    }
                }
            }

            // Convert NV21 to JPEG
            val yuvImage = YuvImage(nv21, ImageFormat.NV21, image.width, image.height, null)

            // Make sure the rectangle is valid
            if (image.width <= 0 || image.height <= 0) {
                Log.e("YuvConverter", "Invalid image dimensions: ${image.width}x${image.height}")
                throw IllegalArgumentException("Invalid image dimensions")
            }

            yuvImage.compressToJpeg(Rect(0, 0, image.width, image.height), 90, out)

            val imageBytes = out.toByteArray()
            Log.d("YuvConverter", "JPEG byte array size: ${imageBytes.size}")

            // Decode JPEG to Bitmap
            val tempBitmap = android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            if (tempBitmap == null) {
                Log.e("YuvConverter", "Failed to decode JPEG to Bitmap")
                throw RuntimeException("Failed to decode JPEG to Bitmap")
            }

            // Copy the bitmap data to the output bitmap
            val canvas = android.graphics.Canvas(output)
            canvas.drawBitmap(tempBitmap, 0f, 0f, null)

            tempBitmap.recycle()
            Log.d("YuvConverter", "Conversion successful")

        } catch (e: Exception) {
            Log.e("YuvConverter", "Error converting YUV to RGB", e)
            throw e
        }
    }
}
