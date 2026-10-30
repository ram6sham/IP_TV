package com.example.ui.screens

import android.content.Intent
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sports
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.model.LiveStream
import com.example.ui.components.AdSenseBannerView
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
import com.example.util.AdSenseConfig

@Composable
fun HomeScreen(
  currentStream: LiveStream?,
  activeServerUrl: String,
  selectedServerName: String,
  liveMatches: List<LiveStream>,
  recommendedMatches: List<RecommendedStream>,
  isAdFree: Boolean,
  remainingAdFreeTime: String,
  adSenseConfig: AdSenseConfig,
  isFullscreen: Boolean = false,
  isAdminMode: Boolean = false,
  onSelectServer: (serverName: String, url: String) -> Unit,
  onLikeStream: () -> Unit,
  onSelectLiveMatch: (LiveStream) -> Unit,
  onOpenAddStreamDialog: () -> Unit,
  onOpenRewardedAd: () -> Unit,
  onSyncRemoteGist: () -> Unit,
  onFullscreenToggle: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current

  if (isFullscreen && currentStream != null) {
    // True Fullscreen Cinema Landscape View
    Box(
      modifier = modifier
        .fillMaxSize()
        .background(Color.Black)
        .testTag("fullscreen_home_container")
    ) {
      VideoPlayer(
        stream = currentStream,
        activeUrl = activeServerUrl,
        selectedServer = selectedServerName,
        onSelectServer = onSelectServer,
        isFullscreen = true,
        onFullscreenToggle = onFullscreenToggle,
        modifier = Modifier.fillMaxSize()
      )
    }
  } else {
    // Normal Vertical View
    Column(
      modifier = modifier
        .fillMaxSize()
        .background(ObsidianBackground)
    ) {
      if (currentStream != null) {
        // Hero Video Player Box
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black)
        ) {
          VideoPlayer(
            stream = currentStream,
            activeUrl = activeServerUrl,
            selectedServer = selectedServerName,
            onSelectServer = onSelectServer,
            isFullscreen = false,
            onFullscreenToggle = onFullscreenToggle
          )
        }

        // Active Stream Info & Server Bar
        Surface(
          color = SurfaceDark,
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
            // Match title and quick actions
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
                    text = "${currentStream.sportCategory.icon} ${currentStream.league.ifBlank { currentStream.sportCategory.displayName }}",
                    fontSize = 11.sp,
                    color = BroadcastCyan,
                    fontWeight = FontWeight.Bold
                  )
                  if (currentStream.score.isNotBlank()) {
                    Text(
                      text = " • ${currentStream.score}",
                      fontSize = 11.sp,
                      color = LiveRed,
                      fontWeight = FontWeight.Bold
                    )
                  }
                }
              }

              // Quick Actions: Share (and Add Match if admin)
              Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                  onClick = {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                      type = "text/plain"
                      putExtra(Intent.EXTRA_SUBJECT, currentStream.title)
                      putExtra(Intent.EXTRA_TEXT, "🔴 Watch \"${currentStream.title}\" live on IP TV!\nStream Link: ${currentStream.streamUrl}")
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Share Match"))
                  },
                  modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(SurfaceVariantDark)
                ) {
                  Icon(Icons.Default.Share, contentDescription = "Share", tint = TextPrimary, modifier = Modifier.size(16.dp))
                }

                if (isAdminMode) {
                  Spacer(modifier = Modifier.width(6.dp))
                  IconButton(
                    onClick = onOpenAddStreamDialog,
                    modifier = Modifier
                      .size(34.dp)
                      .clip(CircleShape)
                      .background(LiveRed)
                  ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Match", tint = Color.White, modifier = Modifier.size(18.dp))
                  }
                }
              }
            }

            // Server Switcher Bar (Requirement 3: Server 1, Server 2, Server 3)
            val servers = currentStream.getAvailableServers()
            if (servers.isNotEmpty()) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = "Active Server:",
                  fontSize = 11.sp,
                  color = TextMuted,
                  fontWeight = FontWeight.SemiBold,
                  modifier = Modifier.padding(end = 8.dp)
                )
                servers.forEach { (serverName, url) ->
                  val isSelected = serverName == selectedServerName
                  Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) LiveRed else SurfaceVariantDark,
                    border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                    modifier = Modifier
                      .padding(end = 6.dp)
                      .clickable { onSelectServer(serverName, url) }
                      .testTag("home_server_chip_${serverName.replace(" ", "_")}")
                  ) {
                    Row(
                      verticalAlignment = Alignment.CenterVertically,
                      modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                      Text(
                        text = if (serverName == "Server 1") "⚡ $serverName" else if (serverName == "Server 2") "🚀 $serverName" else "🛡️ $serverName",
                        color = if (isSelected) Color.White else TextPrimary,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 11.sp
                      )
                    }
                  }
                }
              }
            }
          }
        }

        // AdSense Banner Slot (when not Ad-Free)
        if (!isAdFree && adSenseConfig.isBannerEnabled) {
          AdSenseBannerView(
            publisherId = adSenseConfig.publisherId,
            bannerAdUnitId = adSenseConfig.bannerAdUnitId,
            authorizedDomain = adSenseConfig.authorizedDomain,
            onAdFreeClick = onOpenRewardedAd
          )
        }

        // Scrollable Content: Clean Match Details & Other Matches
        LazyColumn(
          modifier = Modifier
            .weight(1f)
            .fillMaxWidth(),
          contentPadding = PaddingValues(bottom = 24.dp)
        ) {
          // Upcoming Matches (Requirement 4: Other Live Matches changed to Upcoming Matches)
          val upcomingMatches = liveMatches.filter { it.id != currentStream.id }.distinctBy { it.id }
          if (upcomingMatches.isNotEmpty()) {
            item {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Surface(shape = CircleShape, color = LiveRed, modifier = Modifier.size(8.dp)) {}
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = "Upcoming Matches",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = TextPrimary
                  )
                }
                Text(
                  text = "${upcomingMatches.size} Matches",
                  color = TextMuted,
                  fontSize = 11.sp
                )
              }
            }

            items(upcomingMatches, key = { "upcoming_${it.id}" }) { match ->
              Surface(
                shape = RoundedCornerShape(12.dp),
                color = SurfaceDark,
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 14.dp, vertical = 4.dp)
                  .clickable { onSelectLiveMatch(match) }
              ) {
                Row(
                  modifier = Modifier.padding(12.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Surface(
                      shape = CircleShape,
                      color = SurfaceVariantDark,
                      modifier = Modifier.size(36.dp)
                    ) {
                      Box(contentAlignment = Alignment.Center) {
                        Text(match.sportCategory.icon, fontSize = 16.sp)
                      }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                      Text(
                        text = match.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = TextPrimary,
                        maxLines = 1
                      )
                      Text(
                        text = "${match.sportCategory.displayName} • ${match.score.ifBlank { match.matchTime }}",
                        fontSize = 11.sp,
                        color = TextSecondary
                      )
                    }
                  }

                  Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = LiveRed
                  ) {
                    Text(
                      text = "PLAY",
                      color = Color.White,
                      fontWeight = FontWeight.Bold,
                      fontSize = 10.sp,
                      modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                  }
                }
              }
            }
          }

          // Recommended Matches Section (if any)
          val uniqueRecommendations = recommendedMatches
            .filter { it.stream.id != currentStream.id }
            .distinctBy { it.stream.id }
          if (uniqueRecommendations.isNotEmpty()) {
            item {
              Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)) {
                Text(
                  text = "Recommended For You",
                  fontWeight = FontWeight.Bold,
                  fontSize = 14.sp,
                  color = TextPrimary
                )
              }
            }

            items(uniqueRecommendations, key = { "rec_${it.stream.id}" }) { rec ->
              Surface(
                shape = RoundedCornerShape(10.dp),
                color = SurfaceDark,
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 14.dp, vertical = 4.dp)
                  .clickable { onSelectLiveMatch(rec.stream) }
              ) {
                Row(
                  modifier = Modifier.padding(12.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = "${rec.stream.sportCategory.icon} ${rec.stream.title}",
                      fontWeight = FontWeight.Bold,
                      fontSize = 13.sp,
                      color = TextPrimary
                    )
                    Text(
                      text = rec.recommendationReason,
                      fontSize = 11.sp,
                      color = BroadcastCyan,
                      modifier = Modifier.padding(top = 2.dp)
                    )
                  }
                  Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = LiveRed
                  ) {
                    Text(
                      text = "PLAY",
                      color = Color.White,
                      fontWeight = FontWeight.Bold,
                      fontSize = 10.sp,
                      modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                  }
                }
              }
            }
          }
        }
      } else {
        // Empty State: When no matches are loaded or present (Requirement 5)
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
          contentAlignment = Alignment.Center
        ) {
          Surface(
            shape = RoundedCornerShape(16.dp),
            color = SurfaceDark,
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(
              modifier = Modifier.padding(24.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Text(text = "📺", fontSize = 48.sp)
              Spacer(modifier = Modifier.height(10.dp))
              Text(
                text = "No Live Matches Available",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = TextPrimary
              )
              Text(
                text = "Add new matches from the Admin Panel or sync your sports links from GitHub Gist.",
                fontSize = 12.sp,
                color = TextSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(top = 6.dp, bottom = 16.dp)
              )

              Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                  onClick = onSyncRemoteGist,
                  colors = ButtonDefaults.buttonColors(containerColor = BroadcastCyan),
                  shape = RoundedCornerShape(10.dp)
                ) {
                  Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("Gist Sync", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                if (isAdminMode) {
                  Button(
                    onClick = onOpenAddStreamDialog,
                    colors = ButtonDefaults.buttonColors(containerColor = LiveRed),
                    shape = RoundedCornerShape(10.dp)
                  ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Match", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}
