package com.app.facedetectiondemo.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import androidx.camera.core.ImageProxy
import java.io.ByteArrayOutputStream

fun cropFace(bitmap: Bitmap, rect: Rect): Bitmap {
    val x = rect.left.coerceAtLeast(0)
    val y = rect.top.coerceAtLeast(0)
    val width = rect.width().coerceAtMost(bitmap.width - x)
    val height = rect.height().coerceAtMost(bitmap.height - y)
    return Bitmap.createBitmap(bitmap, x, y, width, height)
}

