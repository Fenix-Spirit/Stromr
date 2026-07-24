package com.spiritfenix.stromr.ui

import com.spiritfenix.stromr.data.MediaItem

sealed class UiState {
    object Loading: UiState()
    data class Success(val items: List<MediaItem>): UiState()
    data class Error(val message: String): UiState()
}