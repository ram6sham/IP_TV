package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.LiveStream
import com.example.model.NotificationAlert
import com.example.model.StreamCategory
import com.example.util.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import java.util.concurrent.TimeUnit

class StreamRepository(
  private val context: Context,
  private val scope: CoroutineScope? = null
) {

  private val prefs: SharedPreferences = context.getSharedPreferences("pptv_stream_prefs", Context.MODE_PRIVATE)

  private val httpClient = OkHttpClient.Builder()
    .connectTimeout(10, TimeUnit.SECONDS)
    .readTimeout(10, TimeUnit.SECONDS)
    .build()

  // Default initial live streams
  private val defaultStreams = listOf(
    LiveStream(
      id = "stream_pptv_official",
      title = "PP TV 24/7 Live Stream HD",
      description = "Official 24/7 live transmission of PP TV featuring breaking news, entertainment, and cultural specials.",
      streamUrl = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8",
      category = StreamCategory.NEWS,
      broadcasterName = "PP TV Studios",
      viewersCount = 18450,
      isLive = true,
      isBreaking = true,
      tags = listOf("Live", "Breaking", "Nepal", "HD"),
      thumbnailUrl = "",
      likesCount = 2840,
      resolution = "1080p60"
    ),
    LiveStream(
      id = "stream_sports_live",
      title = "PP Sports Live Arena",
      description = "Live tournament coverage, commentary, analysis, and sports action in ultra-low latency.",
      streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
      category = StreamCategory.SPORTS,
      broadcasterName = "Sports Network",
      viewersCount = 32800,
      isLive = true,
      isBreaking = false,
      tags = listOf("Sports", "Football", "Cricket", "Arena"),
      thumbnailUrl = "",
      likesCount = 5910,
      resolution = "1080p"
    ),
    LiveStream(
      id = "stream_nepal_music",
      title = "Nepali Beats & Folk Live TV",
      description = "Non-stop Nepali folk, modern pop, and cultural live musical performances.",
      streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
      category = StreamCategory.MUSIC,
      broadcasterName = "PP Music Beats",
      viewersCount = 9240,
      isLive = true,
      isBreaking = false,
      tags = listOf("Music", "NepaliBeats", "Folk", "24/7"),
      thumbnailUrl = "",
      likesCount = 1420,
      resolution = "720p"
    ),
    LiveStream(
      id = "stream_entertainment_hub",
      title = "PP Entertainment Cinema & Comedy",
      description = "Live comedy shows, cinema reviews, celebrity interviews, and lifestyle streams.",
      streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
      category = StreamCategory.ENTERTAINMENT,
      broadcasterName = "Entertainment Hub",
      viewersCount = 14320,
      isLive = true,
      isBreaking = false,
      tags = listOf("Comedy", "Cinema", "TalkShow"),
      thumbnailUrl = "",
      likesCount = 3210,
      resolution = "1080p"
    ),
    LiveStream(
      id = "stream_community_feed",
      title = "Kathmandu & Pokhara City Live Cam",
      description = "24-hour scenic city live stream views, weather telemetry, and community updates.",
      streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
      category = StreamCategory.COMMUNITY,
      broadcasterName = "Nepal Live Cam",
      viewersCount = 6870,
      isLive = true,
      isBreaking = false,
      tags = listOf("Nepal", "Kathmandu", "Pokhara", "Scenic"),
      thumbnailUrl = "",
      likesCount = 980,
      resolution = "1080p"
    )
  )

  private val _streams = MutableStateFlow<List<LiveStream>>(emptyList())
  val streams: StateFlow<List<LiveStream>> = _streams.asStateFlow()

  private val _currentStream = MutableStateFlow<LiveStream?>(null)
  val currentStream: StateFlow<LiveStream?> = _currentStream.asStateFlow()

  // In-app alert broadcast for newly added/updated streams
  private val _broadcastAlert = MutableSharedFlow<NotificationAlert>(extraBufferCapacity = 5)
  val broadcastAlert: SharedFlow<NotificationAlert> = _broadcastAlert.asSharedFlow()

  // Recently watched stream IDs for personalization
  private val _watchHistory = MutableStateFlow<List<String>>(emptyList())
  val watchHistory: StateFlow<List<String>> = _watchHistory.asStateFlow()

  // Admin / Broadcaster Mode Protection
  private val _isAdminMode = MutableStateFlow(false)
  val isAdminMode: StateFlow<Boolean> = _isAdminMode.asStateFlow()

  // Remote Config Auto-Update URL
  private val _remoteConfigUrl = MutableStateFlow(prefs.getString("pptv_remote_url", "") ?: "")
  val remoteConfigUrl: StateFlow<String> = _remoteConfigUrl.asStateFlow()

  private val _remoteSyncStatus = MutableStateFlow<String?>(null)
  val remoteSyncStatus: StateFlow<String?> = _remoteSyncStatus.asStateFlow()

  init {
    loadStreams()
    startPeriodicAutoSync()
  }

  private fun loadStreams() {
    val customStreams = loadCustomStreamsFromPrefs()
    val combined = (customStreams + defaultStreams).distinctBy { it.id }
    _streams.value = combined
    if (_currentStream.value == null && combined.isNotEmpty()) {
      _currentStream.value = combined.first()
    }
  }

  fun selectStream(stream: LiveStream) {
    _currentStream.value = stream
    addToWatchHistory(stream.id)
  }

  fun selectStreamById(streamId: String) {
    val found = _streams.value.find { it.id == streamId }
    if (found != null) {
      selectStream(found)
    }
  }

  private fun addToWatchHistory(streamId: String) {
    val current = _watchHistory.value.toMutableList()
    current.remove(streamId)
    current.add(0, streamId)
    _watchHistory.value = current.take(10)
  }

  /**
   * Admin Mode Verification:
   * Protects adding links or broadcasting from normal viewers.
   */
  fun verifyAdminPin(enteredPin: String): Boolean {
    val storedPin = prefs.getString("pptv_admin_pin", "1234") ?: "1234"
    val isValid = (enteredPin.trim() == storedPin.trim())
    if (isValid) {
      _isAdminMode.value = true
    }
    return isValid
  }

  fun lockAdminMode() {
    _isAdminMode.value = false
  }

  fun setAdminPin(newPin: String) {
    prefs.edit().putString("pptv_admin_pin", newPin.trim()).apply()
  }

  /**
   * Save and broadcast custom stream link.
   */
  fun addAndBroadcastStream(
    title: String,
    streamUrl: String,
    category: StreamCategory,
    broadcasterName: String,
    description: String,
    notifyInstantly: Boolean = true,
    autoPlayNow: Boolean = true
  ): LiveStream {
    val streamId = "custom_stream_" + UUID.randomUUID().toString().take(8)
    val newStream = LiveStream(
      id = streamId,
      title = title.ifBlank { "Live Stream on PP TV" },
      description = description.ifBlank { "User broadcasted live stream link on PP TV." },
      streamUrl = streamUrl.trim(),
      category = category,
      broadcasterName = broadcasterName.ifBlank { "Broadcaster" },
      viewersCount = 1,
      isLive = true,
      isBreaking = true,
      tags = listOf("New", "LiveNow", category.displayName),
      createdAt = System.currentTimeMillis(),
      isCustomUserStream = true,
      likesCount = 12
    )

    // Save locally
    val existing = loadCustomStreamsFromPrefs().toMutableList()
    existing.add(0, newStream)
    saveCustomStreamsToPrefs(existing)

    // Update state flow
    val all = (listOf(newStream) + _streams.value).distinctBy { it.id }
    _streams.value = all

    // If autoPlayNow is true, instantly switch active stream so everyone's app watches it automatically
    if (autoPlayNow) {
      selectStream(newStream)
    }

    // Trigger instant notification
    if (notifyInstantly) {
      NotificationHelper.sendLiveStreamNotification(context, newStream)

      val alert = NotificationAlert(
        id = UUID.randomUUID().toString(),
        streamId = newStream.id,
        streamTitle = newStream.title,
        message = "🔴 New stream link saved! Live broadcast started: \"${newStream.title}\".",
        broadcasterName = newStream.broadcasterName
      )
      _broadcastAlert.tryEmit(alert)
    }

    return newStream
  }

  /**
   * Auto Update Configuration:
   * Sets Remote URL (e.g. GitHub Gist JSON, Firebase, or custom server endpoint)
   * where admin can update the master stream links for all apps automatically.
   */
  fun setRemoteConfigUrl(url: String) {
    val cleanUrl = url.trim()
    _remoteConfigUrl.value = cleanUrl
    prefs.edit().putString("pptv_remote_url", cleanUrl).apply()
    if (cleanUrl.isNotBlank()) {
      scope?.launch {
        syncRemoteStreams(cleanUrl)
      }
    }
  }

  /**
   * Automatically polls the Remote URL to sync stream links on all mobile devices.
   */
  suspend fun syncRemoteStreams(targetUrl: String? = null): Boolean {
    val url = targetUrl ?: _remoteConfigUrl.value
    if (url.isBlank()) return false

    return withContext(Dispatchers.IO) {
      try {
        _remoteSyncStatus.value = "Checking remote live links..."
        val request = Request.Builder()
          .url(url)
          .header("Cache-Control", "no-cache")
          .build()

        val response = httpClient.newCall(request).execute()
        if (!response.isSuccessful) {
          _remoteSyncStatus.value = "Remote sync error (HTTP ${response.code})"
          return@withContext false
        }

        val jsonString = response.body?.string() ?: return@withContext false
        val updatedStreams = parseRemoteStreamsJson(jsonString)

        if (updatedStreams.isNotEmpty()) {
          withContext(Dispatchers.Main) {
            applyRemoteStreams(updatedStreams)
          }
          _remoteSyncStatus.value = "Synced ${updatedStreams.size} live streams automatically"
          true
        } else {
          _remoteSyncStatus.value = "No valid streams found in remote response"
          false
        }
      } catch (e: Exception) {
        _remoteSyncStatus.value = "Sync failed: ${e.localizedMessage ?: "Network error"}"
        false
      }
    }
  }

  private fun parseRemoteStreamsJson(jsonString: String): List<LiveStream> {
    val result = mutableListOf<LiveStream>()
    try {
      val trimmed = jsonString.trim()
      val array = if (trimmed.startsWith("[")) {
        JSONArray(trimmed)
      } else if (trimmed.startsWith("{")) {
        val root = JSONObject(trimmed)
        root.optJSONArray("streams") ?: JSONArray()
      } else {
        return emptyList()
      }

      for (i in 0 until array.length()) {
        val obj = array.getJSONObject(i)
        val id = obj.optString("id", "remote_${System.currentTimeMillis()}_$i")
        val title = obj.optString("title", "PP TV Live")
        val streamUrl = obj.optString("streamUrl", obj.optString("url", ""))
        if (streamUrl.isNotBlank()) {
          val catName = obj.optString("category", "NEWS")
          val category = try {
            StreamCategory.valueOf(catName)
          } catch (_: Exception) {
            StreamCategory.NEWS
          }
          result.add(
            LiveStream(
              id = id,
              title = title,
              description = obj.optString("description", "Remote live stream feed"),
              streamUrl = streamUrl,
              category = category,
              broadcasterName = obj.optString("broadcasterName", "PP TV Broadcaster"),
              viewersCount = obj.optInt("viewersCount", 12000),
              isLive = obj.optBoolean("isLive", true),
              isBreaking = obj.optBoolean("isBreaking", false),
              likesCount = obj.optInt("likesCount", 100),
              isCustomUserStream = true
            )
          )
        }
      }
    } catch (_: Exception) {
      // JSON Parse issue
    }
    return result
  }

  private fun applyRemoteStreams(remoteList: List<LiveStream>) {
    val current = _currentStream.value
    var activeStreamUpdated = false

    // Merge remote streams into stream list
    val currentList = _streams.value.toMutableList()
    remoteList.forEach { remoteStream ->
      val index = currentList.indexOfFirst { it.id == remoteStream.id }
      if (index >= 0) {
        val oldStream = currentList[index]
        if (oldStream.streamUrl != remoteStream.streamUrl) {
          currentList[index] = remoteStream
          if (current?.id == remoteStream.id) {
            _currentStream.value = remoteStream
            activeStreamUpdated = true
          }
        }
      } else {
        currentList.add(0, remoteStream)
      }
    }

    _streams.value = currentList

    if (activeStreamUpdated) {
      _currentStream.value?.let { updated ->
        NotificationHelper.sendLiveStreamNotification(context, updated)
        _broadcastAlert.tryEmit(
          NotificationAlert(
            id = UUID.randomUUID().toString(),
            streamId = updated.id,
            streamTitle = updated.title,
            message = "🔴 Live stream link updated automatically from remote server!",
            broadcasterName = updated.broadcasterName
          )
        )
      }
    }
  }

  private fun startPeriodicAutoSync() {
    scope?.launch(Dispatchers.IO) {
      while (isActive) {
        delay(60_000) // Check every 60 seconds
        if (_remoteConfigUrl.value.isNotBlank()) {
          syncRemoteStreams()
        }
      }
    }
  }

  fun toggleLikeCurrentStream() {
    val current = _currentStream.value ?: return
    val updated = current.copy(likesCount = current.likesCount + 1)
    _currentStream.value = updated
    _streams.value = _streams.value.map { if (it.id == updated.id) updated else it }
  }

  private fun saveCustomStreamsToPrefs(list: List<LiveStream>) {
    val array = JSONArray()
    list.forEach { stream ->
      val obj = JSONObject().apply {
        put("id", stream.id)
        put("title", stream.title)
        put("description", stream.description)
        put("streamUrl", stream.streamUrl)
        put("category", stream.category.name)
        put("broadcasterName", stream.broadcasterName)
        put("viewersCount", stream.viewersCount)
        put("isLive", stream.isLive)
        put("isBreaking", stream.isBreaking)
        put("likesCount", stream.likesCount)
      }
      array.put(obj)
    }
    prefs.edit().putString("saved_custom_streams", array.toString()).apply()
  }

  private fun loadCustomStreamsFromPrefs(): List<LiveStream> {
    val raw = prefs.getString("saved_custom_streams", null) ?: return emptyList()
    val list = mutableListOf<LiveStream>()
    try {
      val array = JSONArray(raw)
      for (i in 0 until array.length()) {
        val obj = array.getJSONObject(i)
        val catName = obj.optString("category", "CUSTOM")
        val category = try {
          StreamCategory.valueOf(catName)
        } catch (_: Exception) {
          StreamCategory.CUSTOM
        }
        list.add(
          LiveStream(
            id = obj.getString("id"),
            title = obj.getString("title"),
            description = obj.optString("description", ""),
            streamUrl = obj.getString("streamUrl"),
            category = category,
            broadcasterName = obj.optString("broadcasterName", "Broadcaster"),
            viewersCount = obj.optInt("viewersCount", 1),
            isLive = obj.optBoolean("isLive", true),
            isBreaking = obj.optBoolean("isBreaking", false),
            likesCount = obj.optInt("likesCount", 10),
            isCustomUserStream = true
          )
        )
      }
    } catch (_: Exception) {
      // Ignore parse error
    }
    return list
  }
}
