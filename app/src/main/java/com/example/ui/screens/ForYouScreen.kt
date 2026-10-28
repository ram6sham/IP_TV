package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.RecommendedStream
import com.example.model.LiveStream
import com.example.ui.theme.BroadcastCyan
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.LiveRed
import com.example.ui.theme.ObsidianBackground
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceHighlight
import com.example.ui.theme.SurfaceVariantDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun ForYouScreen(
  recommendations: List<RecommendedStream>,
  allStreams: List<LiveStream>,
  onSelectStream: (LiveStream) -> Unit,
  modifier: Modifier = Modifier
) {
  val topPick = recommendations.firstOrNull()
  val trendingList = allStreams.sortedByDescending { it.viewersCount }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(ObsidianBackground)
      .testTag("for_you_screen"),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(18.dp)
  ) {
    // Header
    item {
      Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = "Personalized",
            tint = BroadcastCyan,
            modifier = Modifier.size(24.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Personalized For You",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = TextPrimary
          )
        }
        Text(
          text = "Tailored live streams and recommendations based on your viewing habits",
          fontSize = 12.sp,
          color = TextSecondary,
          modifier = Modifier.padding(top = 2.dp)
        )
      }
    }

    // Hero Top Recommendation Card
    if (topPick != null) {
      item {
        Surface(
          shape = RoundedCornerShape(18.dp),
          color = SurfaceDark,
          modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelectStream(topPick.stream) }
            .testTag("top_pick_card")
        ) {
          Box {
            // Gradient Header
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .background(
                  Brush.horizontalGradient(
                    listOf(ElectricIndigo.copy(alpha = 0.5f), BroadcastCyan.copy(alpha = 0.3f))
                  )
                )
            )

            Column(modifier = Modifier.padding(16.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Surface(
                  shape = RoundedCornerShape(20.dp),
                  color = LiveRed
                ) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                  ) {
                    Icon(
                      imageVector = Icons.Default.AutoAwesome,
                      contentDescription = null,
                      tint = Color.White,
                      modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                      text = "TOP RECOMMENDATION",
                      color = Color.White,
                      fontWeight = FontWeight.Black,
                      fontSize = 10.sp
                    )
                  }
                }

                Surface(
                  shape = RoundedCornerShape(12.dp),
                  color = BroadcastCyan
                ) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                  ) {
                    Icon(
                      imageVector = Icons.Default.Star,
                      contentDescription = null,
                      tint = Color.Black,
                      modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                      text = "${topPick.matchPercentage}% Match",
                      color = Color.Black,
                      fontWeight = FontWeight.Bold,
                      fontSize = 11.sp
                    )
                  }
                }
              }

              Spacer(modifier = Modifier.height(16.dp))

              Text(
                text = topPick.stream.title,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = TextPrimary
              )
              Text(
                text = topPick.recommendationReason,
                fontSize = 12.sp,
                color = BroadcastCyan,
                modifier = Modifier.padding(top = 2.dp)
              )
              Text(
                text = topPick.stream.description,
                fontSize = 12.sp,
                color = TextSecondary,
                maxLines = 2,
                modifier = Modifier.padding(top = 6.dp)
              )

              Spacer(modifier = Modifier.height(14.dp))

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = "🔴 ${topPick.stream.viewersCount / 1000}k watching now",
                  fontSize = 12.sp,
                  color = LiveRed,
                  fontWeight = FontWeight.SemiBold
                )

                Surface(
                  shape = RoundedCornerShape(20.dp),
                  color = BroadcastCyan
                ) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                  ) {
                    Icon(
                      imageVector = Icons.Default.PlayArrow,
                      contentDescription = "Watch Now",
                      tint = Color.Black,
                      modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Watch Stream", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                  }
                }
              }
            }
          }
        }
      }
    }

    // Personalized Affinity Insights Card
    item {
      Surface(
        shape = RoundedCornerShape(14.dp),
        color = SurfaceVariantDark,
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Text(
            text = "Your Viewing Taste Profile",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = TextPrimary
          )
          Spacer(modifier = Modifier.height(6.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            TasteChip("News & Info", "94% Affinity", modifier = Modifier.weight(1f))
            TasteChip("Sports Arena", "88% Affinity", modifier = Modifier.weight(1f))
            TasteChip("Folk Music", "82% Affinity", modifier = Modifier.weight(1f))
          }
        }
      }
    }

    // Recommended Streams Carousel
    item {
      Column {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "✨ Curated Recommended Live Feeds",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = TextPrimary
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          items(recommendations) { rec ->
            CuratedStreamCard(
              recommendation = rec,
              onClick = { onSelectStream(rec.stream) }
            )
          }
        }
      }
    }

    // Trending Section
    item {
      Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.TrendingUp,
            contentDescription = "Trending",
            tint = LiveRed,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Trending Across PP TV",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = TextPrimary
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          trendingList.take(4).forEach { stream ->
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = SurfaceDark,
              modifier = Modifier
                .fillMaxWidth()
                .clickable { onSelectStream(stream) }
            ) {
              Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.weight(1f)
                ) {
                  Box(
                    modifier = Modifier
                      .size(36.dp)
                      .clip(CircleShape)
                      .background(SurfaceHighlight),
                    contentAlignment = Alignment.Center
                  ) {
                    Text("📺", fontSize = 16.sp)
                  }
                  Spacer(modifier = Modifier.width(10.dp))
                  Column {
                    Text(
                      text = stream.title,
                      fontWeight = FontWeight.Bold,
                      fontSize = 13.sp,
                      color = TextPrimary,
                      maxLines = 1
                    )
                    Text(
                      text = "${stream.broadcasterName} • ${stream.category.displayName}",
                      fontSize = 11.sp,
                      color = TextSecondary
                    )
                  }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(
                    text = "${stream.viewersCount / 1000}k live",
                    fontSize = 11.sp,
                    color = LiveRed,
                    fontWeight = FontWeight.Bold
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = BroadcastCyan,
                    modifier = Modifier.size(18.dp)
                  )
                }
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun TasteChip(
  label: String,
  percentage: String,
  modifier: Modifier = Modifier
) {
  Surface(
    shape = RoundedCornerShape(8.dp),
    color = SurfaceHighlight,
    modifier = modifier
  ) {
    Column(modifier = Modifier.padding(8.dp)) {
      Text(label, fontSize = 11.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold, maxLines = 1)
      Text(percentage, fontSize = 10.sp, color = BroadcastCyan)
    }
  }
}

@Composable
fun CuratedStreamCard(
  recommendation: RecommendedStream,
  onClick: () -> Unit
) {
  Surface(
    shape = RoundedCornerShape(14.dp),
    color = SurfaceDark,
    modifier = Modifier
      .width(220.dp)
      .clickable { onClick() }
      .testTag("curated_stream_${recommendation.stream.id}")
  ) {
    Column {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(100.dp)
          .background(SurfaceHighlight),
        contentAlignment = Alignment.Center
      ) {
        Text("📺", fontSize = 32.sp)

        Surface(
          shape = RoundedCornerShape(6.dp),
          color = BroadcastCyan,
          modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(6.dp)
        ) {
          Text(
            text = "${recommendation.matchPercentage}% Match",
            color = Color.Black,
            fontWeight = FontWeight.Bold,
            fontSize = 9.sp,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
          )
        }

        Surface(
          shape = RoundedCornerShape(4.dp),
          color = LiveRed,
          modifier = Modifier
            .align(Alignment.BottomStart)
            .padding(6.dp)
        ) {
          Text(
            text = "LIVE",
            color = Color.White,
            fontWeight = FontWeight.Black,
            fontSize = 8.sp,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
          )
        }
      }

      Column(modifier = Modifier.padding(10.dp)) {
        Text(
          text = recommendation.stream.title,
          fontWeight = FontWeight.Bold,
          fontSize = 13.sp,
          color = TextPrimary,
          maxLines = 1
        )
        Text(
          text = recommendation.recommendationReason,
          fontSize = 11.sp,
          color = BroadcastCyan,
          maxLines = 1,
          modifier = Modifier.padding(top = 2.dp)
        )
        Text(
          text = "${recommendation.stream.broadcasterName} • ${recommendation.stream.viewersCount / 1000}k",
          fontSize = 10.sp,
          color = TextMuted,
          modifier = Modifier.padding(top = 4.dp)
        )
      }
    }
  }
}
