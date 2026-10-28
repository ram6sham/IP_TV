package com.example.data

import com.example.model.LiveStream
import com.example.model.StreamCategory

data class RecommendedStream(
  val stream: LiveStream,
  val matchPercentage: Int,
  val recommendationReason: String,
  val isTopPick: Boolean = false
)

object RecommendationEngine {

  fun getRecommendations(
    allStreams: List<LiveStream>,
    watchHistoryIds: List<String>,
    currentStreamId: String?
  ): List<RecommendedStream> {
    if (allStreams.isEmpty()) return emptyList()

    // Determine user's preferred categories from history
    val watchedStreams = allStreams.filter { it.id in watchHistoryIds }
    val categoryFrequency = watchedStreams.groupingBy { it.category }.eachCount()
    val topCategory = categoryFrequency.maxByOrNull { it.value }?.key ?: StreamCategory.NEWS

    return allStreams
      .filter { it.id != currentStreamId }
      .mapIndexed { index, stream ->
        var score = 75

        // Boost if matches user's preferred category
        if (stream.category == topCategory) {
          score += 15
        }

        // Boost if breaking or has high viewers
        if (stream.isBreaking) {
          score += 5
        }
        if (stream.viewersCount > 15000) {
          score += 4
        }
        if (stream.isCustomUserStream) {
          score += 5 // newly added community streams
        }

        val clampedScore = (score + (index % 3) * 2).coerceIn(78, 99)

        val reason = when {
          stream.isCustomUserStream -> "Newly Added Live Broadcast"
          stream.category == topCategory -> "Matches your favorite ${stream.category.displayName}"
          stream.isBreaking -> "Breaking live coverage right now"
          stream.viewersCount > 20000 -> "Trending with ${stream.viewersCount / 1000}k+ live viewers"
          else -> "Popular in Nepal live broadcasts"
        }

        RecommendedStream(
          stream = stream,
          matchPercentage = clampedScore,
          recommendationReason = reason,
          isTopPick = index == 0 || clampedScore >= 95
        )
      }
      .sortedByDescending { it.matchPercentage }
  }
}
