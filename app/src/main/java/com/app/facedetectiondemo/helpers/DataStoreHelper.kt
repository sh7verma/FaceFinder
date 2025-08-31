package com.app.facedetectiondemo.helpers

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.app.facedetectiondemo.data.SavedPhoto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class DataStoreHelper(private val context: Context) {
    private val json = Json { ignoreUnknownKeys = true }

    companion object {
        private val Context.photosDataStore by preferencesDataStore(name = "photos")
        private val PHOTOS_KEY = stringPreferencesKey("saved_photos")
        private val CURRENT_PHOTO_KEY = stringPreferencesKey("current_photo")
        private val MATCHER_PHOTO_KEY = stringPreferencesKey("matched_photo")
    }

    val savedPhotos: Flow<List<SavedPhoto>> = context.photosDataStore.data
        .map { preferences ->
            val photosJson = preferences[PHOTOS_KEY] ?: "[]"
            try {
                json.decodeFromString<List<SavedPhoto>>(photosJson)
            } catch (e: Exception) {
                Log.e("DataStoreHelper", "Error decoding photos", e)
                emptyList()
            }
        }

    val currentPhoto: Flow<SavedPhoto?> = context.photosDataStore.data
        .map { preferences ->
            val photosJson = preferences[CURRENT_PHOTO_KEY]
            photosJson?.let { json.decodeFromString<SavedPhoto>(it) }
        }


    val matchedPhoto: Flow<SavedPhoto?> = context.photosDataStore.data
        .map { preferences ->
            val photosJson = preferences[MATCHER_PHOTO_KEY]
            photosJson?.let { json.decodeFromString<SavedPhoto>(it) }
        }

    suspend fun addPhotoToDataStore(photo: SavedPhoto) {
        context.photosDataStore.edit { preferences ->
            val existingPhotosJson = preferences[PHOTOS_KEY] ?: "[]"
            val existingPhotos = try {
                json.decodeFromString<List<SavedPhoto>>(existingPhotosJson)
            } catch (e: Exception) {
                emptyList()
            }
            val updatedPhotos = existingPhotos + photo
            preferences[PHOTOS_KEY] = json.encodeToString(updatedPhotos)
        }
    }

    suspend fun deletePhotoFromDataStore(photoId: String): SavedPhoto? {
        var deletedPhoto: SavedPhoto? = null
        context.photosDataStore.edit { preferences ->
            val existingPhotosJson = preferences[PHOTOS_KEY] ?: "[]"
            val existingPhotos = try {
                json.decodeFromString<List<SavedPhoto>>(existingPhotosJson)
            } catch (e: Exception) {
                emptyList()
            }
            deletedPhoto = existingPhotos.find { it.id == photoId }
            val updatedPhotos = existingPhotos.filter { it.id != photoId }
            preferences[PHOTOS_KEY] = json.encodeToString(updatedPhotos)
        }

        return deletedPhoto
    }

    suspend fun saveCurrentPhotoId(photo: SavedPhoto) {
        context.photosDataStore.edit { preferences ->
            preferences[CURRENT_PHOTO_KEY] = json.encodeToString(photo)
        }
    }

    suspend fun saveMatchedPhotoId(photo: SavedPhoto) {
        context.photosDataStore.edit { preferences ->
            preferences[MATCHER_PHOTO_KEY] = json.encodeToString(photo)
        }
    }

    suspend fun deleteMatchedPhoto() {
        context.photosDataStore.edit { preferences ->
            preferences.remove(MATCHER_PHOTO_KEY)
        }
    }
}
