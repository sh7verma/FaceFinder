package com.app.facedetectiondemo.repo

import android.content.Context
import android.graphics.Bitmap
import com.app.facedetectiondemo.data.SavedPhoto
import com.app.facedetectiondemo.helpers.DataStoreHelper
import kotlinx.coroutines.flow.Flow
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class PhotoRepository(private val context: Context) {
    private val dataStoreHelper = DataStoreHelper(context)

    suspend fun savePhoto(
        bitmap: Bitmap,
        faceId: String? = null,
        embedding: List<Float> = emptyList()
    ): SavedPhoto {
        val photoId = UUID.randomUUID().toString()
        val fileName = "photo_$photoId.jpg"
        val photosDir = File(context.filesDir, "photos")
        if (!photosDir.exists()) {
            photosDir.mkdirs()
        }
        val photoFile = File(photosDir, fileName)
        FileOutputStream(photoFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
        val savedPhoto = SavedPhoto(
            id = photoId,
            filePath = photoFile.absolutePath,
            faceId = faceId,
            embedding = embedding
        )
        dataStoreHelper.addPhotoToDataStore(savedPhoto)
        return savedPhoto
    }

    val savedPhotos: Flow<List<SavedPhoto>> = dataStoreHelper.savedPhotos

    val currentPhoto: Flow<SavedPhoto?> = dataStoreHelper.currentPhoto

    val matchedPhoto: Flow<SavedPhoto?> = dataStoreHelper.matchedPhoto

    suspend fun saveCurrentPhoto(
        bitmap: Bitmap,
        faceId: String? = null,
        embedding: List<Float> = emptyList()
    ) {
        val photoId = UUID.randomUUID().toString()
        val fileName = "photo_$photoId.jpg"


        val photosDir = File(context.filesDir, "photos")
        if (!photosDir.exists()) {
            photosDir.mkdirs()
        }


        val photoFile = File(photosDir, fileName)
        FileOutputStream(photoFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }


        val savedPhoto = SavedPhoto(
            id = photoId,
            filePath = photoFile.absolutePath,
            faceId = faceId,
            embedding = embedding
        )

        dataStoreHelper.saveCurrentPhotoId(savedPhoto)
    }

    suspend fun saveMatchedPhoto(photo: SavedPhoto) {
        dataStoreHelper.saveMatchedPhotoId(photo)
    }

    suspend fun deletePhoto(photoId: String) {
        val photoToDelete = dataStoreHelper.deletePhotoFromDataStore(photoId)

        photoToDelete?.let {
            val file = File(it.filePath)
            if (file.exists()) {
                file.delete()
            }
        }
    }

    suspend fun deleteMatchedPhoto() {
        dataStoreHelper.deleteMatchedPhoto()
    }
}