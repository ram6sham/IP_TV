package com.example.data

import com.example.model.LiveStream
import com.example.model.SportCategory

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

    val watchedStreams = allStreams.filter { it.id in watchHistoryIds }
    val sportFrequency = watchedStreams.groupingBy { it.sportCategory }.eachCount()
    val topSport = sportFrequency.maxByOrNull { it.value }?.key ?: SportCategory.CRICKET

    return allStreams
      .filter { it.id != currentStreamId }
      .mapIndexed { index, stream ->
        var score = 75

        if (stream.sportCategory == topSport) {
          score += 15
        }
        if (stream.isBreaking) {
          score += 5
        }
        if (stream.viewersCount > 25000) {
          score += 4
        }
        if (stream.isCustomUserStream) {
          score += 5
        }

        val clampedScore = (score + (index % 3) * 2).coerceIn(78, 99)

        val reason = when {
          stream.isCustomUserStream -> "Newly Added Live Match"
          stream.sportCategory == topSport -> "Matches your interest in ${stream.sportCategory.displayName}"
          stream.isBreaking -> "High Stakes Live Match"
          stream.viewersCount > 30000 -> "Trending Match"
          else -> "Popular live stream"
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
