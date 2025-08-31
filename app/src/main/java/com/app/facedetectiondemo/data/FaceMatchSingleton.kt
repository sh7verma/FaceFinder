package com.app.facedetectiondemo.data

/**
 * Singleton class to store face IDs and their similarity values with the clicked photo
 */
object FaceMatchSingleton {
    
    /**
     * Data class to hold face ID and its similarity value
     */
    data class FaceMatch(val faceId: String, val similarity: Float)
    
    // List to store face matches
    private val _faceMatches = mutableListOf<FaceMatch>()
    
    // Public read-only access to face matches
    val faceMatches: List<FaceMatch>
        get() = _faceMatches.toList()
    
    /**
     * Add a face match to the list
     * @param faceId ID of the face
     * @param similarity Similarity value with the clicked photo
     */
    fun addFaceMatch(faceId: String, similarity: Float) {
        _faceMatches.add(FaceMatch(faceId, similarity))
    }
    
    /**
     * Clear all face matches
     */
    fun clearFaceMatches() {
        _faceMatches.clear()
    }
}