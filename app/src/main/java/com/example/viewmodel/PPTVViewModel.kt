package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ChatRepository
import com.example.data.RecommendationEngine
import com.example.data.RecommendedStream
import com.example.data.StreamRepository
import com.example.model.ChatMessage
import com.example.model.FloatingReaction
import com.example.model.LiveStream
import com.example.model.NotificationAlert
import com.example.model.StreamCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class MainNavTab(val title: String) {
  LIVE_TV("Live TV"),
  CHANNELS("Channels"),
  FOR_YOU("For You"),
  SETTINGS("Settings"),
  BROADCAST("Broadcast Studio")
}

class PPTVViewModel(application: Application) : AndroidViewModel(application) {

  val streamRepository = StreamRepository(application, viewModelScope)
  val chatRepository = ChatRepository(viewModelScope)

  val streams: StateFlow<List<LiveStream>> = streamRepository.streams
  val currentStream: StateFlow<LiveStream?> = streamRepository.currentStream
  val messages: StateFlow<List<ChatMessage>> = chatRepository.messages
  val reactions = chatRepository.reactions

  val isAdminMode: StateFlow<Boolean> = streamRepository.isAdminMode
  val remoteConfigUrl: StateFlow<String> = streamRepository.remoteConfigUrl
  val remoteSyncStatus: StateFlow<String?> = streamRepository.remoteSyncStatus

  private val _currentTab = MutableStateFlow(MainNavTab.LIVE_TV)
  val currentTab: StateFlow<MainNavTab> = _currentTab.asStateFlow()

  private val _searchQuery = MutableStateFlow("")
  val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

  private val _selectedCategoryFilter = MutableStateFlow(StreamCategory.ALL)
  val selectedCategoryFilter: StateFlow<StreamCategory> = _selectedCategoryFilter.asStateFlow()

  private val _showAddStreamDialog = MutableStateFlow(false)
  val showAddStreamDialog: StateFlow<Boolean> = _showAddStreamDialog.asStateFlow()

  private val _showAdminLoginDialog = MutableStateFlow(false)
  val showAdminLoginDialog: StateFlow<Boolean> = _showAdminLoginDialog.asStateFlow()

  private val _showRemoteConfigDialog = MutableStateFlow(false)
  val showRemoteConfigDialog: StateFlow<Boolean> = _showRemoteConfigDialog.asStateFlow()

  private val _isFullscreen = MutableStateFlow(false)
  val isFullscreen: StateFlow<Boolean> = _isFullscreen.asStateFlow()

  private val _activeAlert = MutableStateFlow<NotificationAlert?>(null)
  val activeAlert: StateFlow<NotificationAlert?> = _activeAlert.asStateFlow()

  private var logoTapCount = 0
  private var lastLogoTapTime = 0L

  val filteredStreams: StateFlow<List<LiveStream>> = combine(
    streams,
    _searchQuery,
    _selectedCategoryFilter
  ) { list, query, category ->
    list.filter { stream ->
      val matchesCategory = category == StreamCategory.ALL || stream.category == category
      val matchesQuery = query.isBlank() ||
          stream.title.contains(query, ignoreCase = true) ||
          stream.broadcasterName.contains(query, ignoreCase = true) ||
          stream.tags.any { it.contains(query, ignoreCase = true) }
      matchesCategory && matchesQuery
    }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val personalizedRecommendations: StateFlow<List<RecommendedStream>> = combine(
    streams,
    streamRepository.watchHistory,
    currentStream
  ) { all, history, current ->
    RecommendationEngine.getRecommendations(all, history, current?.id)
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  init {
    viewModelScope.launch {
      currentStream.collect { stream ->
        if (stream != null) {
          chatRepository.switchStream(stream.id, stream.title, stream.broadcasterName)
        }
      }
    }

    viewModelScope.launch {
      streamRepository.broadcastAlert.collect { alert ->
        _activeAlert.value = alert
      }
    }
  }

  fun setTab(tab: MainNavTab) {
    _currentTab.value = tab
  }

  fun setSearchQuery(query: String) {
    _searchQuery.value = query
  }

  fun setCategoryFilter(category: StreamCategory) {
    _selectedCategoryFilter.value = category
  }

  fun selectStream(stream: LiveStream) {
    streamRepository.selectStream(stream)
    _currentTab.value = MainNavTab.LIVE_TV
  }

  fun selectStreamById(streamId: String) {
    streamRepository.selectStreamById(streamId)
    _currentTab.value = MainNavTab.LIVE_TV
  }

  fun setIsFullscreen(fullscreen: Boolean) {
    _isFullscreen.value = fullscreen
  }

  fun toggleFullscreen() {
    _isFullscreen.value = !_isFullscreen.value
  }

  fun openAddStreamDialog() {
    if (isAdminMode.value) {
      _showAddStreamDialog.value = true
    } else {
      _showAdminLoginDialog.value = true
    }
  }

  fun closeAddStreamDialog() {
    _showAddStreamDialog.value = false
  }

  fun openAdminLoginDialog() {
    _showAdminLoginDialog.value = true
  }

  fun closeAdminLoginDialog() {
    _showAdminLoginDialog.value = false
  }

  fun openRemoteConfigDialog() {
    _showRemoteConfigDialog.value = true
  }

  fun closeRemoteConfigDialog() {
    _showRemoteConfigDialog.value = false
  }

  fun onLogoClicked() {
    val now = System.currentTimeMillis()
    if (now - lastLogoTapTime < 800) {
      logoTapCount++
    } else {
      logoTapCount = 1
    }
    lastLogoTapTime = now

    // Tapping 5 times unlocks Admin Login Modal
    if (logoTapCount >= 5) {
      logoTapCount = 0
      openAdminLoginDialog()
    }
  }

  fun verifyAdminPin(pin: String): Boolean {
    val ok = streamRepository.verifyAdminPin(pin)
    if (ok) {
      _showAdminLoginDialog.value = false
      _currentTab.value = MainNavTab.BROADCAST
    }
    return ok
  }

  fun lockAdminMode() {
    streamRepository.lockAdminMode()
    if (_currentTab.value == MainNavTab.BROADCAST) {
      _currentTab.value = MainNavTab.LIVE_TV
    }
  }

  fun setRemoteConfigUrl(url: String) {
    streamRepository.setRemoteConfigUrl(url)
  }

  fun syncRemoteStreamsNow() {
    viewModelScope.launch {
      streamRepository.syncRemoteStreams()
    }
  }

  fun dismissAlert() {
    _activeAlert.value = null
  }

  fun saveAndBroadcastStream(
    title: String,
    streamUrl: String,
    category: StreamCategory,
    broadcasterName: String,
    description: String,
    notifyInstantly: Boolean = true,
    autoPlayNow: Boolean = true
  ) {
    streamRepository.addAndBroadcastStream(
      title = title,
      streamUrl = streamUrl,
      category = category,
      broadcasterName = broadcasterName,
      description = description,
      notifyInstantly = notifyInstantly,
      autoPlayNow = autoPlayNow
    )
    _showAddStreamDialog.value = false
    if (autoPlayNow) {
      _currentTab.value = MainNavTab.LIVE_TV
    }
  }

  fun sendChatMessage(text: String) {
    chatRepository.sendMessage(text)
  }

  fun triggerReaction(emoji: String) {
    chatRepository.triggerReaction(emoji)
  }

  fun likeCurrentStream() {
    streamRepository.toggleLikeCurrentStream()
    triggerReaction("❤️")
  }

  fun likeChatMessage(messageId: String) {
    chatRepository.likeMessage(messageId)
  }
}
