package com.veyra.tv.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.veyra.tv.data.AppDatabase
import com.veyra.tv.data.ChannelRepository
import com.veyra.tv.data.DefaultPlaylists
import com.veyra.tv.data.M3uParser
import com.veyra.tv.model.Channel
import com.veyra.tv.model.Playlist
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository: ChannelRepository
    private val prefs = application.getSharedPreferences("iptv_prefs", Context.MODE_PRIVATE)
    
    // UI State
    private val _searchQuery = MutableStateFlow("")
    private val _selectedCategory = MutableStateFlow<String?>(null)
    private val _isSyncing = MutableStateFlow(false)
    private val _playbackUrl = MutableStateFlow<String?>(null)
    private val _fallbackQueue = MutableStateFlow<List<Channel>>(emptyList())
    private val _userCountry = MutableStateFlow<String?>(null)
    private val _selectedCountry = MutableStateFlow<String?>(null)

    val searchQuery: StateFlow<String> = _searchQuery
    val selectedCategory: StateFlow<String?> = _selectedCategory
    val isSyncing: StateFlow<Boolean> = _isSyncing
    val playbackUrl: StateFlow<String?> = _playbackUrl
    val userCountry: StateFlow<String?> = _userCountry
    val selectedCountry: StateFlow<String?> = _selectedCountry
    
    val allPlaylists: StateFlow<List<Playlist>>
    val selectedPlaylist: StateFlow<Playlist?>

    private var parsingJob: Job? = null

    init {
        val playlistDao = database.playlistDao()
        val channelDao = database.channelDao()
        repository = ChannelRepository(channelDao, playlistDao, M3uParser())
        
        allPlaylists = repository.allPlaylists.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        selectedPlaylist = repository.selectedPlaylist.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

        val savedCategory = prefs.getString("default_category", null)
        _selectedCategory.value = savedCategory
        
        val savedCountry = prefs.getString("selected_country", "DETECTED")
        if (savedCountry != "ALL") {
             if (savedCountry != "DETECTED") {
                 _selectedCountry.value = savedCountry
             }
        }

        viewModelScope.launch {
            val country = com.veyra.tv.data.CountryRepository.getUserCountry()
            _userCountry.value = country
            if (savedCountry == "DETECTED" && country != null) {
                _selectedCountry.value = country
            }
        }
        
        setupDefaultPlaylists()
    }

    val availableCountries: Flow<List<String>> = selectedPlaylist.flatMapLatest { playlist ->
        if (playlist == null) flowOf(emptyList()) else repository.countries(playlist.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: Flow<List<String>> = combine(selectedPlaylist, _selectedCountry) { playlist, country ->
        playlist to country
    }.flatMapLatest { (playlist, country) ->
        if (playlist == null) flowOf(emptyList()) else repository.categories(playlist.id, country)
    }

    val settingsCategories: StateFlow<List<String>> = selectedPlaylist.flatMapLatest { playlist ->
        if (playlist == null) flowOf(emptyList()) else repository.categories(playlist.id, null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()) // Simplified

    val recentChannels: StateFlow<List<Channel>> = selectedPlaylist.flatMapLatest { playlist ->
        if (playlist == null) flowOf(emptyList()) else repository.recentChannels(playlist.id, 10)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pagedChannels: Flow<PagingData<Channel>> = combine(
        selectedPlaylist,
        _selectedCategory,
        _searchQuery,
        _selectedCountry
    ) { playlist, category, query, country ->
        PlaylistState(playlist, category, query, country)
    }.flatMapLatest { state ->
        val config = PagingConfig(pageSize = 20, enablePlaceholders = true, initialLoadSize = 40, prefetchDistance = 10)
        
        if (state.query.isNotEmpty()) {
             // Global search across all playlists
            Pager(config) { database.channelDao().searchAllChannelsPagingSource(state.query) }.flow
        } else {
             if (state.playlist == null) {
                return@flatMapLatest flowOf(PagingData.empty())
            }

            if (state.category != null && state.category != "Favorites") {
                Pager(config) { database.channelDao().getChannelsByCategoryAndCountryPagingSource(state.category, state.playlist.id, state.country) }.flow
            } else if (state.category == "Favorites") {
                Pager(config) { database.channelDao().getFavoriteChannelsPagingSource(state.playlist.id) }.flow
            } else {
                Pager(config) { database.channelDao().getAllChannelsByCountryPagingSource(state.playlist.id, state.country) }.flow
            }
        }
    }.cachedIn(viewModelScope)

    data class PlaylistState(
        val playlist: Playlist?,
        val category: String?,
        val query: String,
        val country: String?
    )

    private val priorityCategories = listOf("News", "Sports", "Movies", "Kids", "Entertainment", "Music", "Documentary")

    // Navigation Events
    private val _navigationEvent = kotlinx.coroutines.channels.Channel<Pair<String, String>>(kotlinx.coroutines.channels.Channel.BUFFERED)
    val navigationEvent = _navigationEvent.receiveAsFlow()

    private fun setupDefaultPlaylists() {
        viewModelScope.launch {
            // 1. Get existing playlists to avoid duplicates
            val currentPlaylists = repository.allPlaylists.first()
            val defaultPlaylists = DefaultPlaylists.getPlaylists()
            
            val newPlaylists = defaultPlaylists.filter { item ->
                currentPlaylists.none { it.url == item.url }
            }
            
            if (newPlaylists.isNotEmpty()) {
                repository.setupDefaultPlaylists(newPlaylists)
            }

            val jsonUrl = "https://shyam-vadgama.github.io/iptv-json-parser/channels.json"
            
            // Re-fetch to get the ID
            val allPlaylistsNow = repository.allPlaylists.first()
            val fastPlaylist = allPlaylistsNow.find { it.url == jsonUrl }

            // 4. Select and Sync
            var currentSelected = repository.selectedPlaylist.first()
            
            if (currentSelected == null && fastPlaylist != null) {
                repository.selectPlaylist(fastPlaylist.id)
                currentSelected = fastPlaylist
            }
            
            if (currentSelected != null) {
                 // Check if channels need syncing for the active playlist
                val count = database.channelDao().getChannelCountForPlaylist(currentSelected.id)
                if (count == 0) {
                    syncPlaylist(currentSelected)
                }
            }
            
            // 5. Background Indexing for Search & Fallback
            startBackgroundIndexing()
        }
    }
    
    private fun startBackgroundIndexing() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val all = repository.allPlaylists.first()
            // Prioritize the specific URLs requested for fallback
            val targets = all.filter {
                !it.isSelected && (it.url == "internal://debug" || it.url.contains("apsattv") || it.url.contains("Free-TV") || it.url.contains("tvpass") || it.url.contains("epghub") || it.url.contains("PiratesTv"))
            }

            for (playlist in targets) {
                // Yield to ensure UI thread gets priority if this is sharing a pool (though it's IO)
                kotlinx.coroutines.yield()
                
                // Check if already populated
                val count = database.channelDao().getChannelCountForPlaylist(playlist.id)
                if (count == 0) {
                     try {
                        // Sync quietly (don't update UI state)
                        repository.streamAndSaveChannels(playlist)
                        // Add a delay between heavy parsing jobs to let the device cool down/catch up
                        kotlinx.coroutines.delay(2000)
                     } catch (e: Exception) {
                         e.printStackTrace()
                     }
                }
            }
        }
    }
    
    fun syncPlaylist(playlist: Playlist) {
        if (isSyncing.value) return
        parsingJob?.cancel()
        parsingJob = viewModelScope.launch {
            _isSyncing.value = true
            try {
                repository.streamAndSaveChannels(playlist)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isSyncing.value = false
            }
        }
    }

    fun selectAndSyncPlaylist(playlist: Playlist) {
        viewModelScope.launch {
            repository.selectPlaylist(playlist.id)
            if (database.channelDao().getChannelCountForPlaylist(playlist.id) == 0) {
                syncPlaylist(playlist)
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onCategorySelected(category: String?) {
        _selectedCategory.value = category
    }

        fun toggleFavorite(channel: Channel) {
            viewModelScope.launch {
                repository.toggleFavorite(channel.streamUrl, !channel.isFavorite)
            }
        }
    
        fun onChannelSelected(channel: Channel) {
            _playbackUrl.value = channel.streamUrl
            _fallbackQueue.value = emptyList() // Clear previous fallback queue
            viewModelScope.launch {
                repository.updateLastWatched(channel.streamUrl)
            }
        }
        fun onPlaybackFailed(channelName: String, streamUrl: String) {
        viewModelScope.launch {
            // If the queue is empty, populate it with new alternatives
            if (_fallbackQueue.value.isEmpty()) {
                val cleanName = sanitizeChannelName(channelName)
                val alternatives = repository.findAlternativeChannels(cleanName, streamUrl)
                _fallbackQueue.value = alternatives
            }

            // Try the next one in the queue
            val nextInQueue = _fallbackQueue.value.firstOrNull()
            if (nextInQueue != null) {
                _playbackUrl.value = nextInQueue.streamUrl
                // Trigger navigation
                _navigationEvent.send(nextInQueue.streamUrl to nextInQueue.name)
                
                // Remove the one we just tried from the queue
                _fallbackQueue.value = _fallbackQueue.value.drop(1)
            } else {
                // No more fallbacks
                _playbackUrl.value = null // Signal permanent failure
            }
        }
    }

    private fun sanitizeChannelName(name: String): String {
        return name.replace(Regex("\\(.*?\\)"), "") // Remove parenthetical info like (Source 1)
                   .replace(Regex("\\bHD\\b", RegexOption.IGNORE_CASE), "")
                   .replace(Regex("\\bFHD\\b", RegexOption.IGNORE_CASE), "")
                   .replace(Regex("\\b4K\\b", RegexOption.IGNORE_CASE), "")
                   .replace(Regex("\\bHEVC\\b", RegexOption.IGNORE_CASE), "")
                   .trim()
    }

    fun getDefaultCategory(): String? = prefs.getString("default_category", null)

    fun saveDefaultCategory(category: String?) {
        prefs.edit().putString("default_category", category).apply()
    }

    fun setSelectedCountry(countryCode: String?) {
        _selectedCountry.value = countryCode
        if (countryCode == null) {
            prefs.edit().putString("selected_country", "ALL").apply()
        } else {
            prefs.edit().putString("selected_country", countryCode).apply()
        }
    }
}