package com.spiritfenix.stromr.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.spiritfenix.stromr.R
import com.spiritfenix.stromr.data.MediaItem
import com.spiritfenix.stromr.data.RssParser
import com.spiritfenix.stromr.network.rssApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException

private const val TEST_FEED_URL = "https://changelog.com/podcast/feed"
/**
 * ViewModel for MediaItems. Contains a list of MediaItems.
 */
class MediaViewModel(application: Application): AndroidViewModel(application) {
    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()//read-only

    init {
        loadFeed()
    }

    private fun loadFeed() {
        _uiState.value = UiState.Loading
        viewModelScope.launch {
            val context = getApplication<Application>()
            try {
                val xml = rssApiClient.api.fetchFeed(TEST_FEED_URL).string()
                val episodes = RssParser.parse(xml)
                _uiState.value = UiState.Success(episodes)
            } catch (e: IOException) {
                _uiState.value = UiState.Error(context.getString(R.string.fetch_error))
            } catch (e: Exception) {
                _uiState.value = UiState.Error(context.getString(R.string.default_fetch_error))
            }
        }
    }

    fun findById(id: Int): MediaItem? {
        val state = _uiState.value
        return if (state is UiState.Success) state.items.find { it.id == id } else null
    }
}