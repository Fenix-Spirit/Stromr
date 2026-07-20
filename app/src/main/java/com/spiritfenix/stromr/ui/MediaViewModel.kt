package com.spiritfenix.stromr.ui

import androidx.lifecycle.ViewModel
import com.spiritfenix.stromr.data.MediaItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel for MediaItems. Contains a list of MediaItems.
 */
class MediaViewModel: ViewModel() {
    private val _items = MutableStateFlow<List<MediaItem>>(emptyList())
    val items: StateFlow<List<MediaItem>> = _items.asStateFlow()//read-only

    init {
        loadItems()
    }

    private fun loadItems() {
        _items.value = sampleEpisodes
    }
    fun FindById(id:Int): MediaItem? =items.value.find { it.id == id }
}