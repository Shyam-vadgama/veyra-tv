package com.example.iptv_player.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.iptv_player.data.M3uParser
import com.example.iptv_player.model.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private val parser = M3uParser()
    private val _channels = MutableStateFlow<List<Channel>>(emptyList())
    private val _searchQuery = MutableStateFlow("")
    private val _selectedCategory = MutableStateFlow<String?>(null)
    private val _isLoading = MutableStateFlow(false)

    val searchQuery: StateFlow<String> = _searchQuery
    val selectedCategory: StateFlow<String?> = _selectedCategory
    val isLoading: StateFlow<Boolean> = _isLoading

    // Define priority categories for sorting
    private val priorityCategories = listOf("News", "Sports", "Movies", "Kids", "Entertainment", "Music", "Documentary")

    val categories: StateFlow<List<String>> = _channels
        .combine(_channels) { channels, _ ->
            val allCategories = channels.map { it.category }.distinct()
            // Sort: Priority categories first, then others alphabetically
            allCategories.sortedWith(Comparator { c1, c2 ->
                val idx1 = priorityCategories.indexOf(c1)
                val idx2 = priorityCategories.indexOf(c2)
                when {
                    idx1 != -1 && idx2 != -1 -> idx1.compareTo(idx2)
                    idx1 != -1 -> -1
                    idx2 != -1 -> 1
                    else -> c1.compareTo(c2)
                }
            })
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredChannels: StateFlow<List<Channel>> = combine(
        _channels,
        _searchQuery,
        _selectedCategory
    ) { channels, query, category ->
        channels.filter { channel ->
            (category == null || channel.category == category) &&
            (query.isEmpty() || channel.name.contains(query, ignoreCase = true))
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadChannels()
    }

    fun loadChannels() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Using a comprehensive list or the one provided
                val channels = parser.parse("https://iptv-org.github.io/iptv/index.m3u")
                _channels.value = channels
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onCategorySelected(category: String?) {
        _selectedCategory.value = category
    }
}
