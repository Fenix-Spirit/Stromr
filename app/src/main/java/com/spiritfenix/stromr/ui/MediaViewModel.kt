package com.spiritfenix.stromr.ui

import androidx.lifecycle.ViewModel
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
class MediaViewModel: ViewModel() {
    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()//read-only

    init {
        loadFeed()
    }

    private fun loadFeed() {
        _uiState.value = UiState.Loading
        viewModelScope.launch {
            try {
                val xml = rssApiClient.api.fetchFeed(TEST_FEED_URL).string()
                val episodes = RssParser.parse(xml)
                _uiState.value = UiState.Success(episodes)
            } catch (e: IOException) {
                _uiState.value = UiState.Error(R.string.fetch_error.toString())
            } catch (e: Exception) {
                _uiState.value = UiState.Error(R.string.default_fetch_error.toString())
            }
        }
    }

    fun findById(id: Int): MediaItem? {
        val state = _uiState.value
        return if (state is UiState.Success) state.items.find { it.id == id } else null
    }
}