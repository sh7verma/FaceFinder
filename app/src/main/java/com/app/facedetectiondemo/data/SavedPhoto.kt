package com.app.facedetectiondemo.data

import kotlinx.serialization.Serializable

@Serializable
data class SavedPhoto(
    val id: String,
    val filePath: String,
    val faceId: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val embedding: List<Float> = emptyList(), // Using List<Float> instead of FloatArray for serialization compatibility
)
