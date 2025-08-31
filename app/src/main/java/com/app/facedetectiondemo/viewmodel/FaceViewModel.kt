package com.app.facedetectiondemo.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.facedetectiondemo.repo.FaceRepository
import com.app.facedetectiondemo.repo.PhotoRepository
import com.app.facedetectiondemo.data.SavedPhoto
import com.app.facedetectiondemo.helpers.FaceDetectorHelper
import com.app.facedetectiondemo.helpers.FaceNetHelper
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


class FaceViewModel(application: Application) : AndroidViewModel(application) {

    private val faceDetectorHelper = FaceDetectorHelper()
    private val faceNetHelper by lazy { FaceNetHelper(application.applicationContext) }
    private val faceRepository = FaceRepository(application.applicationContext)
    private val photoRepository = PhotoRepository(application.applicationContext)

    override fun onCleared() {
        super.onCleared()
        faceNetHelper.close() // Free native resources when ViewModel is destroyed
    }

    val savedPhotos: StateFlow<List<SavedPhoto>> = photoRepository.savedPhotos
        .stateIn(
            viewModelScope,
            SharingStarted.Lazily,
            emptyList()
        )

    private val _navigateToComparison = MutableSharedFlow<Unit>()
    val navigateToComparison = _navigateToComparison.asSharedFlow()

    var croppedFaceCallback: ((Bitmap?) -> Unit)? = null

    fun processImage(bitmap: Bitmap) {
        Log.d("FaceViewModel", "processImage called")
        viewModelScope.launch {
            deleteMatchedPhoto()
            Log.d("FaceViewModel", "Launching face detection")
            faceDetectorHelper.detectFaces(bitmap, onResult = { faces ->
                Log.d("FaceViewModel", "Faces detected: ${faces.size}")
                if (faces.isEmpty()) {
                    Log.d("FaceViewModel", "No face detected")
                    croppedFaceCallback?.invoke(null)
                    return@detectFaces
                }

                val face = faces.first()
                Log.d("FaceViewModel", "Cropping face: ${face.boundingBox}")
                val cropped = faceDetectorHelper.cropFace(bitmap, face.boundingBox)
                croppedFaceCallback?.invoke(cropped)


                Log.d("FaceViewModel", "Getting embedding")
                val embedding = faceNetHelper.getEmbedding(cropped)

                viewModelScope.launch {
                    Log.d("FaceViewModel", "Matching or creating ID")
                    val result = faceRepository.matchOrCreateId(embedding)

                    photoRepository.saveCurrentPhoto(bitmap, result.id, embedding.toList())

                    if (result.isNew) {
                        val savedPhoto =
                            photoRepository.savePhoto(bitmap, result.id, embedding.toList())
                        Log.d(
                            "FaceViewModel",
                            "New face registered: ${result.id}, saved photo ID: ${savedPhoto.id}"
                        )
                    } else {
                        result.matchedPhoto?.let { photoRepository.saveMatchedPhoto(it) }
                        Log.d("FaceViewModel", "Recognized: ${result.id}")
                    }
                    _navigateToComparison.emit(Unit)
                }
            }, onError = {
                Log.e("FaceViewModel", "Face detection error", it)
                croppedFaceCallback?.invoke(null)
            })
        }
    }


    fun deleteMatchedPhoto() {
        Log.d("FaceViewModel", "Deleting matched photo")
        viewModelScope.launch {
            photoRepository.deleteMatchedPhoto()
            Log.d("FaceViewModel", "Matched photo deleted successfully")
        }
    }
}
