package com.spiritfenix.stromr.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.spiritfenix.stromr.R
import com.spiritfenix.stromr.data.MediaItem
import com.spiritfenix.stromr.data.PodcastRepository
import com.spiritfenix.stromr.data.SubscriptionStore
import com.spiritfenix.stromr.data.local.AppDatabase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

/**
 * ViewModel for MediaItems. Contains a list of MediaItems.
 */
class MediaViewModel(application: Application): AndroidViewModel(application) {
    private val repository = PodcastRepository(AppDatabase.getInstance(application), SubscriptionStore(application))
    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()//read-only
	val subscribedFeeds: StateFlow<Set<String>> = repository.subscribedFeeds.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())
    private var hasAttemptedRefresh = false
	private val _subscribeError = MutableStateFlow<String?>(null)
	val subscribeError: StateFlow<String?> = _subscribeError.asStateFlow()

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

	fun clearSubscribeError() {
		_subscribeError.value = null
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
				if (_uiState.value is UiState.Loading) {
					_uiState.value = UiState.Success(emptyList())
				}
            }
        }
    }
	fun subscribe(url: String, onSuccess: () -> Unit = {}) {
		viewModelScope.launch {
			try {
				repository.subscribe(url.trim())
				onSuccess()
			} catch (e: CancellationException) {
				throw e
			} catch (e: Exception) {
				_subscribeError.value = getApplication<Application>().getString(R.string.subscribe_error)
			}
		}
	}

	fun unsubscribe(url: String) {
		viewModelScope.launch { repository.unsubscribe(url) }
	}
    fun findById(id: String): MediaItem? {
        val state = _uiState.value
        return if (state is UiState.Success) state.items.find { it.id == id } else null
    }
}