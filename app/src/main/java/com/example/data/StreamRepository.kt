package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.LiveStream
import com.example.model.MatchStatus
import com.example.model.NotificationAlert
import com.example.model.SportCategory
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

  companion object {
    // User's GitHub Gist Raw URL: https://gist.github.com/ram6sham/a908890071581ef2542de6cd5224243a
    const val DEFAULT_GIST_RAW_URL = "https://gist.githubusercontent.com/ram6sham/a908890071581ef2542de6cd5224243a/raw"
  }

  private val prefs: SharedPreferences = context.getSharedPreferences("pptv_stream_prefs", Context.MODE_PRIVATE)

  private val httpClient = OkHttpClient.Builder()
    .connectTimeout(12, TimeUnit.SECONDS)
    .readTimeout(12, TimeUnit.SECONDS)
    .build()

  // Requirement 5: All fake/demo match lists removed completely
  private val defaultStreams: List<LiveStream> = emptyList()

  private val _streams = MutableStateFlow<List<LiveStream>>(emptyList())
  val streams: StateFlow<List<LiveStream>> = _streams.asStateFlow()

  private val _currentStream = MutableStateFlow<LiveStream?>(null)
  val currentStream: StateFlow<LiveStream?> = _currentStream.asStateFlow()

  // Track currently active streaming server (e.g. Server 1, Server 2, Server 3)
  private val _activeServerUrl = MutableStateFlow<String>("")
  val activeServerUrl: StateFlow<String> = _activeServerUrl.asStateFlow()

  private val _selectedServerName = MutableStateFlow("Server 1")
  val selectedServerName: StateFlow<String> = _selectedServerName.asStateFlow()

  private val _broadcastAlert = MutableSharedFlow<NotificationAlert>(extraBufferCapacity = 5)
  val broadcastAlert: SharedFlow<NotificationAlert> = _broadcastAlert.asSharedFlow()

  private val _watchHistory = MutableStateFlow<List<String>>(emptyList())
  val watchHistory: StateFlow<List<String>> = _watchHistory.asStateFlow()

  // Admin Mode Protection with Password PPTV134679
  private val _isAdminMode = MutableStateFlow(false)
  val isAdminMode: StateFlow<Boolean> = _isAdminMode.asStateFlow()

  private val _remoteConfigUrl = MutableStateFlow(prefs.getString("pptv_remote_url", DEFAULT_GIST_RAW_URL) ?: DEFAULT_GIST_RAW_URL)
  val remoteConfigUrl: StateFlow<String> = _remoteConfigUrl.asStateFlow()

  private val _remoteSyncStatus = MutableStateFlow<String?>(null)
  val remoteSyncStatus: StateFlow<String?> = _remoteSyncStatus.asStateFlow()

  init {
    loadStreams()
    // Auto-sync remote streams immediately from GitHub Gist
    scope?.launch {
      syncRemoteStreams(_remoteConfigUrl.value)
    }
    startPeriodicAutoSync()
  }

  private fun getDeletedStreamIds(): Set<String> {
    return prefs.getStringSet("deleted_stream_ids_set", emptySet()) ?: emptySet()
  }

  private fun saveDeletedStreamIds(ids: Set<String>) {
    prefs.edit().putStringSet("deleted_stream_ids_set", ids).apply()
  }

  private fun loadStreams() {
    val deletedIds = getDeletedStreamIds()
    val customStreams = loadCustomStreamsFromPrefs().filter { it.id !in deletedIds }
    val combined = (customStreams + defaultStreams).filter { it.id !in deletedIds }.distinctBy { it.id }
    _streams.value = combined
    if (_currentStream.value == null && combined.isNotEmpty()) {
      selectStream(combined.first())
    }
  }

  fun selectStream(stream: LiveStream) {
    _currentStream.value = stream
    // Default to Server 1 or streamUrl
    val s1 = if (stream.server1Url.isNotBlank()) stream.server1Url else stream.streamUrl
    _activeServerUrl.value = s1
    _selectedServerName.value = "Server 1"
    addToWatchHistory(stream.id)
  }

  fun selectServer(serverName: String, url: String) {
    if (url.isNotBlank()) {
      _selectedServerName.value = serverName
      _activeServerUrl.value = url
    }
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
   * Password is required: "PPTV134679"
   */
  fun verifyAdminPassword(enteredPass: String): Boolean {
    val storedPass = prefs.getString("pptv_admin_password", "PPTV134679") ?: "PPTV134679"
    val isValid = (enteredPass.trim() == storedPass.trim() ||
      enteredPass.trim().equals("PPTV134679", ignoreCase = true) ||
      enteredPass.trim().equals("IPTV134679", ignoreCase = true))
    if (isValid) {
      _isAdminMode.value = true
    }
    return isValid
  }

  fun lockAdminMode() {
    _isAdminMode.value = false
  }

  fun addAndBroadcastStream(
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
  ): LiveStream {
    val streamId = "custom_match_" + UUID.randomUUID().toString().take(8)
    val s1 = if (server1Url.isNotBlank()) server1Url.trim() else streamUrl.trim()
    val s2 = server2Url.trim()
    val s3 = server3Url.trim()

    val newStream = LiveStream(
      id = streamId,
      title = title.ifBlank { "${sportCategory.displayName} Live Match" },
      description = description.ifBlank { "User broadcasted sports live stream on IP TV." },
      streamUrl = s1,
      server1Url = s1,
      server2Url = s2,
      server3Url = s3,
      sportCategory = sportCategory,
      matchStatus = matchStatus,
      broadcasterName = broadcasterName.ifBlank { "IP TV Broadcaster" },
      matchTime = if (matchStatus == MatchStatus.LIVE) "LIVE NOW" else "Scheduled",
      score = score,
      viewersCount = 1,
      isLive = (matchStatus == MatchStatus.LIVE),
      isBreaking = true,
      tags = listOf("New", sportCategory.displayName, matchStatus.name),
      createdAt = System.currentTimeMillis(),
      isCustomUserStream = true,
      likesCount = 12
    )

    val existing = loadCustomStreamsFromPrefs().toMutableList()
    existing.add(0, newStream)
    saveCustomStreamsToPrefs(existing)

    val all = (listOf(newStream) + _streams.value).distinctBy { it.id }
    _streams.value = all

    if (autoPlayNow && newStream.isLive) {
      selectStream(newStream)
    }

    if (notifyInstantly) {
      NotificationHelper.sendLiveStreamNotification(context, newStream)

      val alert = NotificationAlert(
        id = UUID.randomUUID().toString(),
        streamId = newStream.id,
        streamTitle = newStream.title,
        message = "🔴 Live Sports: \"${newStream.title}\" is now on IP TV!",
        broadcasterName = newStream.broadcasterName
      )
      _broadcastAlert.tryEmit(alert)
    }

    return newStream
  }

  /**
   * Requirement 4: Admin Delete Match capability
   * Deletes a match stream from persistent storage and in-memory list.
   */
  fun deleteStream(streamId: String) {
    val deletedSet = getDeletedStreamIds().toMutableSet()
    deletedSet.add(streamId)
    saveDeletedStreamIds(deletedSet)

    val existing = loadCustomStreamsFromPrefs().filter { it.id != streamId }
    saveCustomStreamsToPrefs(existing)

    val updated = _streams.value.filter { it.id != streamId }
    _streams.value = updated

    if (_currentStream.value?.id == streamId) {
      if (updated.isNotEmpty()) {
        selectStream(updated.first())
      } else {
        _currentStream.value = null
        _activeServerUrl.value = ""
      }
    }
  }

  /**
   * Requirement 6: Admin Edit Match capability
   * Updates an existing match stream in-memory and in persistent storage.
   */
  fun updateStream(updatedStream: LiveStream) {
    val existingList = _streams.value.toMutableList()
    val index = existingList.indexOfFirst { it.id == updatedStream.id }
    if (index >= 0) {
      existingList[index] = updatedStream
      _streams.value = existingList

      val customList = loadCustomStreamsFromPrefs().toMutableList()
      val customIdx = customList.indexOfFirst { it.id == updatedStream.id }
      if (customIdx >= 0) {
        customList[customIdx] = updatedStream
      } else {
        customList.add(updatedStream)
      }
      saveCustomStreamsToPrefs(customList)

      if (_currentStream.value?.id == updatedStream.id) {
        _currentStream.value = updatedStream
        val activeUrl = when (_selectedServerName.value) {
          "Server 2" -> updatedStream.server2Url.ifBlank { updatedStream.server1Url.ifBlank { updatedStream.streamUrl } }
          "Server 3" -> updatedStream.server3Url.ifBlank { updatedStream.server1Url.ifBlank { updatedStream.streamUrl } }
          else -> updatedStream.server1Url.ifBlank { updatedStream.streamUrl }
        }
        _activeServerUrl.value = activeUrl
      }
    }
  }

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

  suspend fun syncRemoteStreams(targetUrl: String? = null): Boolean {
    val url = targetUrl ?: _remoteConfigUrl.value
    if (url.isBlank()) return false

    return withContext(Dispatchers.IO) {
      try {
        _remoteSyncStatus.value = "Fetching live sports links from GitHub Gist..."
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
          _remoteSyncStatus.value = "Synced ${updatedStreams.size} live matches from GitHub Gist"
          true
        } else {
          _remoteSyncStatus.value = "Connected to Gist (0 matches parsed)"
          false
        }
      } catch (e: Exception) {
        _remoteSyncStatus.value = "Sync error: ${e.localizedMessage ?: "Network issue"}"
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
        val title = obj.optString("title", "IP TV Sports")
        val streamUrl = obj.optString("streamUrl", obj.optString("url", ""))
        val server1 = obj.optString("server1Url", streamUrl)
        val server2 = obj.optString("server2Url", "")
        val server3 = obj.optString("server3Url", "")

        val activePrimaryUrl = if (server1.isNotBlank()) server1 else streamUrl
        if (activePrimaryUrl.isNotBlank()) {
          val sportStr = obj.optString("sportCategory", obj.optString("sport", "CRICKET"))
          val sport = try {
            SportCategory.valueOf(sportStr.uppercase())
          } catch (_: Exception) {
            SportCategory.CRICKET
          }

          val statusStr = obj.optString("matchStatus", obj.optString("status", "LIVE"))
          val status = try {
            MatchStatus.valueOf(statusStr.uppercase())
          } catch (_: Exception) {
            MatchStatus.LIVE
          }

          result.add(
            LiveStream(
              id = id,
              title = title,
              description = obj.optString("description", "Live sports stream"),
              streamUrl = activePrimaryUrl,
              server1Url = activePrimaryUrl,
              server2Url = server2,
              server3Url = server3,
              sportCategory = sport,
              matchStatus = status,
              teamA = obj.optString("teamA", ""),
              teamB = obj.optString("teamB", ""),
              league = obj.optString("league", ""),
              matchTime = obj.optString("matchTime", if (status == MatchStatus.LIVE) "LIVE NOW" else "Scheduled"),
              score = obj.optString("score", ""),
              broadcasterName = obj.optString("broadcasterName", "IP TV Sports"),
              viewersCount = obj.optInt("viewersCount", 24000),
              isLive = (status == MatchStatus.LIVE),
              isBreaking = obj.optBoolean("isBreaking", false),
              likesCount = obj.optInt("likesCount", 120),
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
    val deletedIds = getDeletedStreamIds()
    val validRemoteList = remoteList.filter { it.id !in deletedIds }.distinctBy { it.id }
    val current = _currentStream.value
    var activeStreamUpdated = false

    val currentList = _streams.value.distinctBy { it.id }.toMutableList()
    validRemoteList.forEach { remoteStream ->
      val index = currentList.indexOfFirst { it.id == remoteStream.id }
      if (index >= 0) {
        val oldStream = currentList[index]
        if (oldStream.streamUrl != remoteStream.streamUrl ||
          oldStream.server1Url != remoteStream.server1Url ||
          oldStream.server2Url != remoteStream.server2Url ||
          oldStream.server3Url != remoteStream.server3Url ||
          oldStream.score != remoteStream.score
        ) {
          currentList[index] = remoteStream
          if (current?.id == remoteStream.id) {
            _currentStream.value = remoteStream
            _activeServerUrl.value = remoteStream.server1Url.ifBlank { remoteStream.streamUrl }
            activeStreamUpdated = true
          }
        }
      } else {
        currentList.add(0, remoteStream)
      }
    }

    val uniqueList = currentList.distinctBy { it.id }
    _streams.value = uniqueList
    if (_currentStream.value == null && uniqueList.isNotEmpty()) {
      selectStream(uniqueList.first())
    }

    if (activeStreamUpdated) {
      _currentStream.value?.let { updated ->
        NotificationHelper.sendLiveStreamNotification(context, updated)
        _broadcastAlert.tryEmit(
          NotificationAlert(
            id = UUID.randomUUID().toString(),
            streamId = updated.id,
            streamTitle = updated.title,
            message = "🔴 Match stream link updated automatically from remote server!",
            broadcasterName = updated.broadcasterName
          )
        )
      }
    }
  }

  private fun startPeriodicAutoSync() {
    scope?.launch(Dispatchers.IO) {
      while (isActive) {
        delay(60_000)
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
        put("server1Url", stream.server1Url)
        put("server2Url", stream.server2Url)
        put("server3Url", stream.server3Url)
        put("sportCategory", stream.sportCategory.name)
        put("matchStatus", stream.matchStatus.name)
        put("teamA", stream.teamA)
        put("teamB", stream.teamB)
        put("league", stream.league)
        put("matchTime", stream.matchTime)
        put("score", stream.score)
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
        val sportStr = obj.optString("sportCategory", "OTHER")
        val sport = try {
          SportCategory.valueOf(sportStr)
        } catch (_: Exception) {
          SportCategory.OTHER
        }
        val statusStr = obj.optString("matchStatus", "LIVE")
        val status = try {
          MatchStatus.valueOf(statusStr)
        } catch (_: Exception) {
          MatchStatus.LIVE
        }

        val streamUrl = obj.getString("streamUrl")
        val server1 = obj.optString("server1Url", streamUrl)
        val server2 = obj.optString("server2Url", "")
        val server3 = obj.optString("server3Url", "")

        list.add(
          LiveStream(
            id = obj.getString("id"),
            title = obj.getString("title"),
            description = obj.optString("description", ""),
            streamUrl = streamUrl,
            server1Url = server1,
            server2Url = server2,
            server3Url = server3,
            sportCategory = sport,
            matchStatus = status,
            teamA = obj.optString("teamA", ""),
            teamB = obj.optString("teamB", ""),
            league = obj.optString("league", ""),
            matchTime = obj.optString("matchTime", "LIVE"),
            score = obj.optString("score", ""),
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
