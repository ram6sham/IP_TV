package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.RecommendedStream
import com.example.model.ChatMessage
import com.example.model.FloatingReaction
import com.example.model.LiveStream
import com.example.ui.components.FloatingReactionItem
import com.example.ui.components.InteractiveChatView
import com.example.ui.player.VideoPlayer
import com.example.ui.theme.BroadcastCyan
import com.example.ui.theme.LiveRed
import com.example.ui.theme.ObsidianBackground
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceHighlight
import com.example.ui.theme.SurfaceVariantDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.flow.SharedFlow

@Composable
fun LiveTVScreen(
  currentStream: LiveStream?,
  messages: List<ChatMessage>,
  reactionsFlow: SharedFlow<FloatingReaction>,
  recommendedStreams: List<RecommendedStream>,
  isFullscreen: Boolean = false,
  isAdminMode: Boolean = false,
  onSendMessage: (String) -> Unit,
  onTriggerReaction: (String) -> Unit,
  onLikeStream: () -> Unit,
  onLikeMessage: (String) -> Unit,
  onSelectRecommendedStream: (LiveStream) -> Unit,
  onOpenAddStreamDialog: () -> Unit,
  onFullscreenToggle: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var selectedTab by remember { mutableIntStateOf(0) }
  var showLandscapeChatDrawer by remember { mutableStateOf(false) }
  val activeReactions = remember { mutableStateListOf<FloatingReaction>() }

  LaunchedEffect(reactionsFlow) {
    reactionsFlow.collect { reaction ->
      activeReactions.add(reaction)
    }
  }

  if (isFullscreen && currentStream != null) {
    // True Fullscreen Landscape / Cinema View
    Box(
      modifier = modifier
        .fillMaxSize()
        .background(Color.Black)
        .testTag("fullscreen_video_container")
    ) {
      VideoPlayer(
        stream = currentStream,
        isFullscreen = true,
        onFullscreenToggle = onFullscreenToggle,
        modifier = Modifier.fillMaxSize()
      )

      // Floating reactions over video in fullscreen
      activeReactions.forEach { reaction ->
        FloatingReactionItem(
          reaction = reaction,
          onFinished = { id -> activeReactions.removeAll { it.id == id } }
        )
      }

      // Translucent live chat drawer toggle in landscape mode
      Row(
        modifier = Modifier
          .align(Alignment.BottomEnd)
          .padding(end = 60.dp, bottom = 10.dp)
      ) {
        Surface(
          shape = RoundedCornerShape(16.dp),
          color = Color.Black.copy(alpha = 0.65f),
          modifier = Modifier.clickable { showLandscapeChatDrawer = !showLandscapeChatDrawer }
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
          ) {
            Icon(Icons.Default.Chat, contentDescription = "Chat", tint = BroadcastCyan, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = if (showLandscapeChatDrawer) "Hide Chat" else "Live Chat (${messages.size})",
              color = Color.White,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }

      // Floating semi-transparent chat pane when toggled on landscape
      if (showLandscapeChatDrawer) {
        Surface(
          shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp),
          color = SurfaceDark.copy(alpha = 0.92f),
          modifier = Modifier
            .align(Alignment.CenterEnd)
            .fillMaxWidth(0.42f)
            .fillMaxSize()
        ) {
          InteractiveChatView(
            messages = messages,
            onSendMessage = onSendMessage,
            onTriggerReaction = onTriggerReaction,
            onLikeMessage = onLikeMessage,
            modifier = Modifier.fillMaxSize()
          )
        }
      }
    }
  } else {
    // Normal Portrait View
    Column(
      modifier = modifier
        .fillMaxSize()
        .background(ObsidianBackground)
    ) {
      if (currentStream != null) {
        // Hero Video Player Box with Floating Reaction Overlays
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black)
        ) {
          VideoPlayer(
            stream = currentStream,
            isFullscreen = false,
            onFullscreenToggle = onFullscreenToggle
          )

          // Floating Reaction Hearts & Emojis
          activeReactions.forEach { reaction ->
            FloatingReactionItem(
              reaction = reaction,
              onFinished = { id ->
                activeReactions.removeAll { it.id == id }
              }
            )
          }
        }

        // Stream Info Summary Strip
        Surface(
          color = SurfaceDark,
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = currentStream.title,
                  fontWeight = FontWeight.Bold,
                  fontSize = 15.sp,
                  color = TextPrimary,
                  maxLines = 1
                )
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.padding(top = 2.dp)
                ) {
                  Text(
                    text = currentStream.broadcasterName,
                    fontSize = 12.sp,
                    color = BroadcastCyan,
                    fontWeight = FontWeight.Medium
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = SurfaceHighlight
                  ) {
                    Text(
                      text = currentStream.category.displayName,
                      fontSize = 10.sp,
                      color = TextSecondary,
                      modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                  }
                }
              }

              // Actions: Like, Share, (and Add Stream Link if Admin)
              Row(verticalAlignment = Alignment.CenterVertically) {
                // Like stream
                Surface(
                  shape = RoundedCornerShape(16.dp),
                  color = SurfaceVariantDark,
                  modifier = Modifier
                    .clickable { onLikeStream() }
                    .testTag("like_stream_button")
                ) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                  ) {
                    Icon(
                      imageVector = Icons.Default.Favorite,
                      contentDescription = "Like",
                      tint = LiveRed,
                      modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                      text = currentStream.likesCount.toString(),
                      fontSize = 12.sp,
                      color = TextPrimary,
                      fontWeight = FontWeight.Bold
                    )
                  }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Share stream link
                IconButton(
                  onClick = {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                      type = "text/plain"
                      putExtra(Intent.EXTRA_SUBJECT, currentStream.title)
                      putExtra(
                        Intent.EXTRA_TEXT,
                        "🔴 Watch \"${currentStream.title}\" live right now on PP TV!\nStream Link: ${currentStream.streamUrl}"
                      )
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Share Live Stream"))
                  },
                  modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(SurfaceVariantDark)
                    .testTag("share_stream_button")
                ) {
                  Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share",
                    tint = TextPrimary,
                    modifier = Modifier.size(18.dp)
                  )
                }

                // Add Stream Quick Button (ONLY VISIBLE IF ADMIN MODE UNLOCKED!)
                if (isAdminMode) {
                  Spacer(modifier = Modifier.width(6.dp))
                  IconButton(
                    onClick = onOpenAddStreamDialog,
                    modifier = Modifier
                      .size(36.dp)
                      .clip(CircleShape)
                      .background(LiveRed)
                      .testTag("quick_add_stream_button")
                  ) {
                    Icon(
                      imageVector = Icons.Default.Add,
                      contentDescription = "Add Stream",
                      tint = Color.White,
                      modifier = Modifier.size(20.dp)
                    )
                  }
                }
              }
            }
          }
        }

        // Content Tabs: Live Chat | For You (Recommendations) | Stream Details
        TabRow(
          selectedTabIndex = selectedTab,
          containerColor = SurfaceDark,
          contentColor = BroadcastCyan,
          indicator = { tabPositions ->
            TabRowDefaults.SecondaryIndicator(
              modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
              color = BroadcastCyan,
              height = 3.dp
            )
          }
        ) {
          Tab(
            selected = selectedTab == 0,
            onClick = { selectedTab = 0 },
            text = {
              Text(
                text = "Live Chat (${messages.size})",
                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                fontSize = 12.sp
              )
            }
          )
          Tab(
            selected = selectedTab == 1,
            onClick = { selectedTab = 1 },
            text = {
              Text(
                text = "For You (${recommendedStreams.size})",
                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                fontSize = 12.sp
              )
            }
          )
          Tab(
            selected = selectedTab == 2,
            onClick = { selectedTab = 2 },
            text = {
              Text(
                text = "About Stream",
                fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal,
                fontSize = 12.sp
              )
            }
          )
        }

        // Tab Content
        when (selectedTab) {
          0 -> {
            // Live Chat
            InteractiveChatView(
              messages = messages,
              onSendMessage = onSendMessage,
              onTriggerReaction = onTriggerReaction,
              onLikeMessage = onLikeMessage,
              modifier = Modifier.weight(1f)
            )
          }
          1 -> {
            // Personalized recommendations under player
            LazyColumn(
              modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(12.dp),
              verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              item {
                Text(
                  text = "✨ Recommended Based On Your Taste",
                  fontWeight = FontWeight.Bold,
                  fontSize = 14.sp,
                  color = TextPrimary
                )
              }
              items(recommendedStreams, key = { it.stream.id }) { rec ->
                RecommendedStreamCard(
                  recommendation = rec,
                  onClick = { onSelectRecommendedStream(rec.stream) }
                )
              }
            }
          }
          2 -> {
            // Stream Details & Diagnostics
            Column(
              modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp)
            ) {
              Text(
                text = "Broadcast Description",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = TextPrimary
              )
              Text(
                text = currentStream.description,
                fontSize = 13.sp,
                color = TextSecondary,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
              )

              Text(
                text = "Stream Technical Link",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = BroadcastCyan
              )
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = SurfaceVariantDark,
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(top = 4.dp, bottom = 16.dp)
              ) {
                Text(
                  text = currentStream.streamUrl,
                  fontSize = 11.sp,
                  color = TextPrimary,
                  modifier = Modifier.padding(10.dp)
                )
              }

              Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = SurfaceVariantDark,
                  modifier = Modifier.weight(1f)
                ) {
                  Column(modifier = Modifier.padding(10.dp)) {
                    Text("Quality", fontSize = 11.sp, color = TextMuted)
                    Text(currentStream.resolution, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                  }
                }
                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = SurfaceVariantDark,
                  modifier = Modifier.weight(1f)
                ) {
                  Column(modifier = Modifier.padding(10.dp)) {
                    Text("Format", fontSize = 11.sp, color = TextMuted)
                    val format = if (currentStream.streamUrl.contains(".m3u8")) "HLS (.m3u8)" else "MP4 / Direct"
                    Text(format, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                  }
                }
              }
            }
          }
        }
      } else {
        // Empty State
        Box(
          modifier = Modifier.fillMaxSize(),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("No active stream selected", color = TextSecondary, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(12.dp))
            if (isAdminMode) {
              Surface(
                shape = RoundedCornerShape(20.dp),
                color = BroadcastCyan,
                modifier = Modifier.clickable { onOpenAddStreamDialog() }
              ) {
                Text(
                  text = "+ Add Stream Link Now",
                  color = Color.Black,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun RecommendedStreamCard(
  recommendation: RecommendedStream,
  onClick: () -> Unit
) {
  Surface(
    shape = RoundedCornerShape(12.dp),
    color = SurfaceDark,
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onClick() }
      .testTag("recommended_stream_${recommendation.stream.id}")
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(10.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(80.dp, 56.dp)
          .clip(RoundedCornerShape(8.dp))
          .background(SurfaceHighlight),
        contentAlignment = Alignment.Center
      ) {
        Text("📺", fontSize = 24.sp)
        Surface(
          shape = RoundedCornerShape(4.dp),
          color = LiveRed,
          modifier = Modifier
            .align(Alignment.TopStart)
            .padding(4.dp)
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

      Spacer(modifier = Modifier.width(12.dp))

      Column(modifier = Modifier.weight(1f)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = recommendation.stream.title,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = TextPrimary,
            maxLines = 1,
            modifier = Modifier.weight(1f)
          )
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = BroadcastCyan.copy(alpha = 0.15f)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
              Icon(imageVector = Icons.Default.Star, contentDescription = "Score", tint = BroadcastCyan, modifier = Modifier.size(12.dp))
              Spacer(modifier = Modifier.width(2.dp))
              Text(
                text = "${recommendation.matchPercentage}%",
                color = BroadcastCyan,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
              )
            }
          }
        }

        Text(
          text = recommendation.recommendationReason,
          fontSize = 11.sp,
          color = TextSecondary,
          maxLines = 1,
          modifier = Modifier.padding(top = 2.dp)
        )

        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.padding(top = 4.dp)
        ) {
          Text(
            text = "${recommendation.stream.broadcasterName} • ${recommendation.stream.viewersCount / 1000}k watching",
            fontSize = 11.sp,
            color = TextMuted
          )
        }
      }
    }
  }
}
