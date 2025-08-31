package com.app.facedetectiondemo.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.facedetectiondemo.data.SavedPhoto
import com.app.facedetectiondemo.repo.PhotoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ComparisonViewModel(application: Application) : AndroidViewModel(application) {

    private val photoRepository = PhotoRepository(application.applicationContext)
    private val _comparisonState = MutableStateFlow<ComparisonState?>(null)
    val comparisonState: StateFlow<ComparisonState?> = _comparisonState.asStateFlow()

    val currentPhoto = photoRepository.currentPhoto
    val matchedPhoto = photoRepository.matchedPhoto


    val savedPhotos: StateFlow<List<SavedPhoto>> = photoRepository.savedPhotos
        .stateIn(
            viewModelScope,
            SharingStarted.Lazily,
            emptyList()
        )


    init {
        viewModelScope.launch {
            currentPhoto.collectLatest { currentPhoto ->
                currentPhoto?.let { photo ->
                    _comparisonState.value = ComparisonState(isLoading = true, currentPhoto = photo)

                    matchedPhoto.collect { matched ->
                        if (matched != null) {
                            _comparisonState.value = ComparisonState(
                                isLoading = false,
                                currentPhoto = photo,
                                matchedPhoto = matched
                            )
                        } else {
                            _comparisonState.value = ComparisonState(
                                isLoading = false,
                                currentPhoto = photo,
                                matchedPhoto = null
                            )
                        }
                    }
                }
            }
        }
    }

    fun resetState() {
        _comparisonState.value = null
    }
}

data class ComparisonState(
    val isLoading: Boolean = false,
    val currentPhoto: SavedPhoto,
    val matchedPhoto: SavedPhoto? = null
)
