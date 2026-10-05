package com.spiritfenix.stromr.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.spiritfenix.stromr.R
import com.spiritfenix.stromr.data.MediaItem
import com.spiritfenix.stromr.data.PodcastRepository
import com.spiritfenix.stromr.data.SubscriptionStore
import com.spiritfenix.stromr.data.local.AppDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException

/**
 * ViewModel for MediaItems. Contains a list of MediaItems.
 */
class MediaViewModel(application: Application): AndroidViewModel(application) {
    private val repository = PodcastRepository(AppDatabase.getInstance(application), SubscriptionStore(application))
    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()//read-only

    private var hasAttemptedRefresh = false

    init {
        viewModelScope.launch {
            repository.episodes.collect { episodes ->
                if (episodes.isNotEmpty() || hasAttemptedRefresh) {
                    _uiState.value = UiState.Success(episodes)
                }
            }
        }
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            try {
                repository.refresh()
            } catch (e: IOException) {
				if (_uiState.value !is UiState.Success) {
		            _uiState.value = UiState.Error(getApplication<Application>().getString(R.string.fetch_error))
				}
			} catch (e: Exception) {
				if (_uiState.value !is UiState.Success) {
		            _uiState.value = UiState.Error(getApplication<Application>().getString(R.string.default_fetch_error))
				}
			} finally {
                hasAttemptedRefresh = true
            }
        }
    }
    fun findById(id: String): MediaItem? {
        val state = _uiState.value
        return if (state is UiState.Success) state.items.find { it.id == id } else null
    }
}