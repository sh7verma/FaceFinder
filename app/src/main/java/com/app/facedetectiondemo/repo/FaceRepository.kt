package com.app.facedetectiondemo.repo

import android.content.Context
import android.util.Log
import com.app.facedetectiondemo.data.FaceMatchSingleton
import com.app.facedetectiondemo.data.SavedPhoto
import com.app.facedetectiondemo.helpers.DataStoreHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.UUID
import kotlin.math.sqrt

data class MatchResult(val id: String, val isNew: Boolean, val matchedPhoto: SavedPhoto?)

class FaceRepository(private val context: Context) {

    private val threshold = 0.6f // Cosine similarity threshold (higher is better)
    private val dataStoreHelper = DataStoreHelper(context)

    val savedFaces: Flow<List<SavedPhoto>> = dataStoreHelper.savedPhotos

    suspend fun matchOrCreateId(newEmbedding: FloatArray): MatchResult {
        Log.d("FaceRepository", "matchOrCreateId called")
        var bestMatchId: String? = null
        var bestSimilarity = -1f
        var matchedFace: SavedPhoto? = null

        // Clear previous face matches
        FaceMatchSingleton.clearFaceMatches()

        // Get all saved faces from DataStore
        val faces = savedFaces.first()

        for (face in faces) {
            // Skip faces without embeddings
            if (face.embedding.isEmpty()) continue

            val existingEmbedding = face.embedding.toFloatArray()
            val similarity = cosineSimilarity(newEmbedding, existingEmbedding)
            Log.d("FaceRepository", "Comparing with ${face.id}, similarity=$similarity")

            // Save face ID and similarity in the singleton
            FaceMatchSingleton.addFaceMatch(face.id, similarity)

            if (similarity > threshold && similarity > bestSimilarity) {
                bestSimilarity = similarity
                bestMatchId = face.id
                matchedFace = face
            }
        }

        return if (bestMatchId != null) {
            Log.i("FaceRepository", "Face matched with ID: $bestMatchId (sim=$bestSimilarity)")
            MatchResult(bestMatchId, isNew = false, matchedFace)
        } else {
            val newId = UUID.randomUUID().toString()
            Log.i("FaceRepository", "New face added with ID: $newId")
            MatchResult(newId, isNew = true, matchedFace)
        }
    }

    private fun cosineSimilarity(a: FloatArray, b: FloatArray): Float {
        Log.d("FaceRepository", "cosineSimilarity called")
        var dotProduct = 0f
        var normA = 0f
        var normB = 0f
        for (i in a.indices) {
            dotProduct += a[i] * b[i]
            normA += a[i] * a[i]
            normB += b[i] * b[i]
        }
        return dotProduct / (sqrt(normA.toDouble()) * sqrt(normB.toDouble())).toFloat()
    }

    private fun List<Float>.toFloatArray(): FloatArray {
        return FloatArray(size) { index -> this[index] }
    }
}
