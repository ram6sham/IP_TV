package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.RecommendationEngine
import com.example.data.RecommendedStream
import com.example.data.StreamRepository
import com.example.model.LiveStream
import com.example.model.MatchStatus
import com.example.model.NotificationAlert
import com.example.model.SportCategory
import com.example.util.AdFreeManager
import com.example.util.AdSenseConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class MainNavTab(val title: String) {
  HOME("Home"),
  FOOTBALL("Football"),
  CRICKET("Cricket"),
  MORE("More"),
  BROADCAST("Admin Panel")
}

class PPTVViewModel(application: Application) : AndroidViewModel(application) {

  val streamRepository = StreamRepository(application, viewModelScope)
  val adFreeManager = AdFreeManager(application)

  val streams: StateFlow<List<LiveStream>> = streamRepository.streams
  val currentStream: StateFlow<LiveStream?> = streamRepository.currentStream
  val activeServerUrl: StateFlow<String> = streamRepository.activeServerUrl
  val selectedServerName: StateFlow<String> = streamRepository.selectedServerName

  val isAdminMode: StateFlow<Boolean> = streamRepository.isAdminMode
  val remoteConfigUrl: StateFlow<String> = streamRepository.remoteConfigUrl
  val remoteSyncStatus: StateFlow<String?> = streamRepository.remoteSyncStatus

  val isAdFree: StateFlow<Boolean> = adFreeManager.isAdFree
  val remainingAdFreeTime: StateFlow<String> = adFreeManager.remainingTimeFormatted
  val adSenseConfig: StateFlow<AdSenseConfig> = adFreeManager.adConfig

  private val _currentTab = MutableStateFlow(MainNavTab.HOME)
  val currentTab: StateFlow<MainNavTab> = _currentTab.asStateFlow()

  private val _searchQuery = MutableStateFlow("")
  val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

  private val _showAddStreamDialog = MutableStateFlow(false)
  val showAddStreamDialog: StateFlow<Boolean> = _showAddStreamDialog.asStateFlow()

  private val _showAdminLoginDialog = MutableStateFlow(false)
  val showAdminLoginDialog: StateFlow<Boolean> = _showAdminLoginDialog.asStateFlow()

  private val _showRemoteConfigDialog = MutableStateFlow(false)
  val showRemoteConfigDialog: StateFlow<Boolean> = _showRemoteConfigDialog.asStateFlow()

  // Requirement 6: Ad dialogs
  private val _showMandatoryPopupAd = MutableStateFlow(false)
  val showMandatoryPopupAd: StateFlow<Boolean> = _showMandatoryPopupAd.asStateFlow()

  private val _showRotationAd = MutableStateFlow(false)
  val showRotationAd: StateFlow<Boolean> = _showRotationAd.asStateFlow()

  private val _showRewardedAdDialog = MutableStateFlow(false)
  val showRewardedAdDialog: StateFlow<Boolean> = _showRewardedAdDialog.asStateFlow()

  private val _isFullscreen = MutableStateFlow(false)
  val isFullscreen: StateFlow<Boolean> = _isFullscreen.asStateFlow()

  private val _activeAlert = MutableStateFlow<NotificationAlert?>(null)
  val activeAlert: StateFlow<NotificationAlert?> = _activeAlert.asStateFlow()

  private var logoTapCount = 0
  private var lastLogoTapTime = 0L
  private var hasTriggeredStartupPopup = false
  private var previousOrientationLandscape: Boolean? = null

  // Currently LIVE matches
  val currentlyLiveMatches: StateFlow<List<LiveStream>> = streams.map { list ->
    list.filter { it.matchStatus == MatchStatus.LIVE }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Football matches (Live & Upcoming)
  val footballStreams: StateFlow<List<LiveStream>> = streams.map { list ->
    list.filter { it.sportCategory == SportCategory.FOOTBALL }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Cricket matches (Live & Upcoming)
  val cricketStreams: StateFlow<List<LiveStream>> = streams.map { list ->
    list.filter { it.sportCategory == SportCategory.CRICKET }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Other Sports (Volleyball, WWE, etc.)
  val otherSportsStreams: StateFlow<List<LiveStream>> = streams.map { list ->
    list.filter {
      it.sportCategory == SportCategory.VOLLYBALL ||
      it.sportCategory == SportCategory.WWE ||
      it.sportCategory == SportCategory.OTHER
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
      streamRepository.broadcastAlert.collect { alert ->
        _activeAlert.value = alert
      }
    }

    // Trigger mandatory 15-second popup ad smoothly after UI initialization if user is not Ad-Free
    viewModelScope.launch {
      kotlinx.coroutines.delay(1500)
      checkAndTriggerStartupAd()
    }
  }

  fun checkAndTriggerStartupAd() {
    if (!hasTriggeredStartupPopup) {
      hasTriggeredStartupPopup = true
      if (!isAdFree.value && adSenseConfig.value.isPopupEnabled) {
        _showMandatoryPopupAd.value = true
      }
    }
  }

  /**
   * Requirement 6: Mobile rotation ad trigger.
   * When phone orientation changes, if not ad-free and rotation ads enabled, show ad.
   */
  fun onOrientationChanged(isLandscape: Boolean) {
    if (previousOrientationLandscape != null && previousOrientationLandscape != isLandscape) {
      if (!isAdFree.value && adSenseConfig.value.isRotationEnabled) {
        _showRotationAd.value = true
      }
    }
    previousOrientationLandscape = isLandscape
  }

  fun setTab(tab: MainNavTab) {
    _currentTab.value = tab
  }

  fun setSearchQuery(query: String) {
    _searchQuery.value = query
  }

  fun selectStream(stream: LiveStream) {
    streamRepository.selectStream(stream)
    _currentTab.value = MainNavTab.HOME
  }

  fun selectStreamById(streamId: String) {
    streamRepository.selectStreamById(streamId)
    _currentTab.value = MainNavTab.HOME
  }

  fun selectServer(serverName: String, url: String) {
    streamRepository.selectServer(serverName, url)
  }

  fun setIsFullscreen(fullscreen: Boolean) {
    _isFullscreen.value = fullscreen
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

  fun openRewardedAdDialog() {
    _showRewardedAdDialog.value = true
  }

  fun closeRewardedAdDialog() {
    _showRewardedAdDialog.value = false
  }

  fun closeMandatoryPopupAd() {
    _showMandatoryPopupAd.value = false
  }

  fun closeRotationAd() {
    _showRotationAd.value = false
  }

  fun grant12HourAdFreePass() {
    adFreeManager.grant12HourPass()
    _showMandatoryPopupAd.value = false
    _showRewardedAdDialog.value = false
    _showRotationAd.value = false
  }

  /**
   * Requirement 5: Logo 7-taps unlocks Admin Panel.
   */
  fun onLogoClicked() {
    val now = System.currentTimeMillis()
    if (now - lastLogoTapTime < 900) {
      logoTapCount++
    } else {
      logoTapCount = 1
    }
    lastLogoTapTime = now

    if (logoTapCount >= 7) {
      logoTapCount = 0
      openAdminLoginDialog()
    }
  }

  /**
   * Requirement 5: Admin Password "PPTV134679".
   */
  fun verifyAdminPassword(password: String): Boolean {
    val ok = streamRepository.verifyAdminPassword(password)
    if (ok) {
      _showAdminLoginDialog.value = false
      _currentTab.value = MainNavTab.BROADCAST
    }
    return ok
  }

  fun lockAdminMode() {
    streamRepository.lockAdminMode()
    if (_currentTab.value == MainNavTab.BROADCAST) {
      _currentTab.value = MainNavTab.HOME
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
    sportCategory: SportCategory,
    matchStatus: MatchStatus,
    broadcasterName: String,
    description: String,
    score: String = "",
    server1Url: String = "",
    server2Url: String = "",
    server3Url: String = "",
    notifyInstantly: Boolean = true,
    autoPlayNow: Boolean = true
  ) {
    streamRepository.addAndBroadcastStream(
      title = title,
      streamUrl = streamUrl,
      sportCategory = sportCategory,
      matchStatus = matchStatus,
      broadcasterName = broadcasterName,
      description = description,
      score = score,
      server1Url = server1Url,
      server2Url = server2Url,
      server3Url = server3Url,
      notifyInstantly = notifyInstantly,
      autoPlayNow = autoPlayNow
    )
    _showAddStreamDialog.value = false
    if (autoPlayNow) {
      _currentTab.value = MainNavTab.HOME
    }
  }

  fun saveAdSenseConfig(
    publisherId: String,
    bannerId: String,
    interstitialId: String,
    rewardedId: String,
    rotationId: String,
    authorizedDomain: String = "https://ram6sham.github.io",
    popupEnabled: Boolean,
    rotationEnabled: Boolean,
    bannerEnabled: Boolean
  ) {
    adFreeManager.saveAdConfig(
      publisherId,
      bannerId,
      interstitialId,
      rewardedId,
      rotationId,
      authorizedDomain,
      popupEnabled,
      rotationEnabled,
      bannerEnabled
    )
  }

  fun likeCurrentStream() {
    streamRepository.toggleLikeCurrentStream()
  }

  fun deleteStream(streamId: String) {
    streamRepository.deleteStream(streamId)
  }

  fun updateStream(updatedStream: LiveStream) {
    streamRepository.updateStream(updatedStream)
  }
}
