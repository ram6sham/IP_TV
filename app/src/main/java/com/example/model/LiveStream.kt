package com.example.model

enum class SportCategory(val displayName: String, val icon: String) {
  CRICKET("Cricket", "🏏"),
  FOOTBALL("Football", "⚽"),
  VOLLYBALL("Volleyball", "🏐"),
  WWE("WWE", "🤼"),
  OTHER("Other Sports", "🏆")
}

enum class MatchStatus(val label: String) {
  LIVE("LIVE"),
  UPCOMING("UPCOMING"),
  FINISHED("ENDED")
}

data class LiveStream(
  val id: String,
  val title: String,
  val description: String = "",
  val streamUrl: String,
  val server1Url: String = streamUrl,
  val server2Url: String = "",
  val server3Url: String = "",
  val sportCategory: SportCategory = SportCategory.OTHER,
  val matchStatus: MatchStatus = MatchStatus.LIVE,
  val teamA: String = "",
  val teamB: String = "",
  val league: String = "",
  val matchTime: String = "Live Now",
  val score: String = "",
  val broadcasterName: String = "IP TV Sports",
  val viewersCount: Int = 12000,
  val isLive: Boolean = true,
  val isBreaking: Boolean = false,
  val tags: List<String> = emptyList(),
  val thumbnailUrl: String = "",
  val createdAt: Long = System.currentTimeMillis(),
  val isCustomUserStream: Boolean = false,
  val resolution: String = "1080p60",
  val likesCount: Int = 120
) {
  /**
   * Returns available streaming servers (Server 1, Server 2, Server 3)
   */
  fun getAvailableServers(): List<Pair<String, String>> {
    val list = mutableListOf<Pair<String, String>>()
    val s1 = if (server1Url.isNotBlank()) server1Url else streamUrl
    if (s1.isNotBlank()) list.add("Server 1" to s1)
    if (server2Url.isNotBlank()) list.add("Server 2" to server2Url)
    if (server3Url.isNotBlank()) list.add("Server 3" to server3Url)
    if (list.isEmpty() && streamUrl.isNotBlank()) {
      list.add("Server 1" to streamUrl)
    }
    return list
  }
}

data class NotificationAlert(
  val id: String,
  val streamId: String,
  val streamTitle: String,
  val message: String,
  val broadcasterName: String,
  val timestamp: Long = System.currentTimeMillis(),
  val isRead: Boolean = false
)
