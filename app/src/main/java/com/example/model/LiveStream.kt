package com.example.model

enum class StreamCategory(val displayName: String, val iconName: String) {
  ALL("All", "Apps"),
  NEWS("News & Info", "Newspaper"),
  ENTERTAINMENT("Entertainment", "LiveTv"),
  SPORTS("Sports HD", "SportsSoccer"),
  MUSIC("Music & Beats", "MusicNote"),
  COMMUNITY("Community & Live", "People"),
  CUSTOM("Custom Streams", "Broadcast")
}

data class LiveStream(
  val id: String,
  val title: String,
  val description: String,
  val streamUrl: String,
  val category: StreamCategory,
  val broadcasterName: String,
  val viewersCount: Int,
  val isLive: Boolean = true,
  val isBreaking: Boolean = false,
  val tags: List<String> = emptyList(),
  val thumbnailUrl: String = "",
  val createdAt: Long = System.currentTimeMillis(),
  val isCustomUserStream: Boolean = false,
  val resolution: String = "1080p60",
  val likesCount: Int = 120
)

data class NotificationAlert(
  val id: String,
  val streamId: String,
  val streamTitle: String,
  val message: String,
  val broadcasterName: String,
  val timestamp: Long = System.currentTimeMillis(),
  val isRead: Boolean = false
)
