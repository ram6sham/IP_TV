package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sports
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.TextButton
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LiveStream
import com.example.model.MatchStatus
import com.example.model.SportCategory
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BroadcastStudioScreen(
  allStreams: List<LiveStream>,
  adSenseConfig: AdSenseConfig,
  onSaveAndBroadcast: (
    title: String,
    streamUrl: String,
    sportCategory: SportCategory,
    matchStatus: MatchStatus,
    broadcasterName: String,
    description: String,
    score: String,
    server1Url: String,
    server2Url: String,
    server3Url: String,
    notifyInstantly: Boolean,
    autoPlayNow: Boolean
  ) -> Unit,
  onSelectStream: (LiveStream) -> Unit,
  onSaveAdSenseConfig: (
    publisherId: String,
    bannerId: String,
    interstitialId: String,
    rewardedId: String,
    rotationId: String,
    authorizedDomain: String,
    popupEnabled: Boolean,
    rotationEnabled: Boolean,
    bannerEnabled: Boolean
  ) -> Unit,
  onDeleteStream: (String) -> Unit = {},
  onUpdateStream: (LiveStream) -> Unit = {},
  onOpenRemoteConfig: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  var editingStreamId by remember { mutableStateOf<String?>(null) }
  var streamTitle by remember { mutableStateOf("") }
  var server1Url by remember { mutableStateOf("") }
  var server2Url by remember { mutableStateOf("") }
  var server3Url by remember { mutableStateOf("") }
  var broadcasterName by remember { mutableStateOf("IP TV Broadcaster") }
  var description by remember { mutableStateOf("") }
  var score by remember { mutableStateOf("") }
  var selectedSport by remember { mutableStateOf(SportCategory.CRICKET) }
  var selectedStatus by remember { mutableStateOf(MatchStatus.LIVE) }
  var notifyInstantly by remember { mutableStateOf(true) }
  var autoPlayNow by remember { mutableStateOf(true) }
  var errorText by remember { mutableStateOf<String?>(null) }
  var successFeedback by remember { mutableStateOf<String?>(null) }
  var streamToDelete by remember { mutableStateOf<LiveStream?>(null) }

  // AdSense Inputs
  var pubId by remember(adSenseConfig) { mutableStateOf(adSenseConfig.publisherId) }
  var bannerId by remember(adSenseConfig) { mutableStateOf(adSenseConfig.bannerAdUnitId) }
  var interstitialId by remember(adSenseConfig) { mutableStateOf(adSenseConfig.interstitialAdUnitId) }
  var rewardedId by remember(adSenseConfig) { mutableStateOf(adSenseConfig.rewardedAdUnitId) }
  var rotationId by remember(adSenseConfig) { mutableStateOf(adSenseConfig.rotationAdUnitId) }
  var authDomain by remember(adSenseConfig) { mutableStateOf(adSenseConfig.authorizedDomain) }
  var popupEnabled by remember(adSenseConfig) { mutableStateOf(adSenseConfig.isPopupEnabled) }
  var rotationEnabled by remember(adSenseConfig) { mutableStateOf(adSenseConfig.isRotationEnabled) }
  var bannerEnabled by remember(adSenseConfig) { mutableStateOf(adSenseConfig.isBannerEnabled) }
  var adConfigSavedFeedback by remember { mutableStateOf<String?>(null) }

  val customBroadcasts = allStreams.filter { it.isCustomUserStream }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(ObsidianBackground)
      .testTag("broadcast_studio_screen"),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Top Admin Header
    item {
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = SurfaceDark,
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Campaign,
                contentDescription = "Admin Broadcast",
                tint = LiveRed,
                modifier = Modifier.size(28.dp)
              )
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = "IP TV Admin Panel",
                  fontWeight = FontWeight.Bold,
                  fontSize = 18.sp,
                  color = TextPrimary
                )
                Text(
                  text = "Multi-Server Broadcast & Google AdSense Control",
                  fontSize = 12.sp,
                  color = BroadcastCyan
                )
              }
            }

            Surface(
              shape = RoundedCornerShape(12.dp),
              color = BroadcastCyan.copy(alpha = 0.15f),
              modifier = Modifier.clickable { onOpenRemoteConfig() }
            ) {
              Text(
                text = "⚡ GitHub Gist Sync",
                color = BroadcastCyan,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
              )
            }
          }

          Text(
            text = "Broadcast live matches with Server 1, Server 2, and Server 3 options. Manage active sports matches and configure Google AdSense monetization.",
            fontSize = 12.sp,
            color = TextSecondary,
            modifier = Modifier.padding(top = 10.dp)
          )
        }
      }
    }

    // Match Publisher Form Card with Multi-Server Support
    item {
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = SurfaceDark,
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          if (editingStreamId != null) {
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = BroadcastCyan.copy(alpha = 0.15f),
              modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(imageVector = Icons.Default.Edit, contentDescription = null, tint = BroadcastCyan, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(text = "Editing Match Mode", fontWeight = FontWeight.Bold, color = BroadcastCyan, fontSize = 12.sp)
                }
                TextButton(
                  onClick = {
                    editingStreamId = null
                    streamTitle = ""
                    server1Url = ""
                    server2Url = ""
                    server3Url = ""
                    score = ""
                    description = ""
                    errorText = null
                    successFeedback = null
                  }
                ) {
                  Text("Cancel", color = TextSecondary, fontSize = 11.sp)
                }
              }
            }
          }

          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.Dns, contentDescription = null, tint = BroadcastCyan, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = if (editingStreamId != null) "Edit Match Stream" else "Publish Match Stream (Multi-Server)",
              fontWeight = FontWeight.Bold,
              fontSize = 15.sp,
              color = TextPrimary
            )
          }

          Spacer(modifier = Modifier.height(12.dp))

          // Server 1 (Primary)
          Text(
            text = "⚡ Server 1 Stream Link (Primary) *",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = BroadcastCyan
          )
          Spacer(modifier = Modifier.height(4.dp))
          OutlinedTextField(
            value = server1Url,
            onValueChange = {
              server1Url = it
              errorText = null
              successFeedback = null
            },
            placeholder = { Text("https://example.com/live/server1.m3u8", color = TextMuted, fontSize = 12.sp) },
            leadingIcon = {
              Icon(imageVector = Icons.Default.Link, contentDescription = null, tint = BroadcastCyan)
            },
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("broadcast_server1_input"),
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = SurfaceVariantDark,
              unfocusedContainerColor = SurfaceVariantDark,
              focusedBorderColor = BroadcastCyan,
              unfocusedBorderColor = Color.Transparent,
              focusedTextColor = TextPrimary,
              unfocusedTextColor = TextPrimary
            ),
            shape = RoundedCornerShape(12.dp)
          )

          Spacer(modifier = Modifier.height(10.dp))

          // Server 2 (Backup / HD)
          Text(
            text = "🚀 Server 2 Stream Link (Backup / HD - Optional)",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
          )
          Spacer(modifier = Modifier.height(4.dp))
          OutlinedTextField(
            value = server2Url,
            onValueChange = { server2Url = it },
            placeholder = { Text("https://example.com/live/server2.m3u8 (Optional)", color = TextMuted, fontSize = 12.sp) },
            leadingIcon = {
              Icon(imageVector = Icons.Default.Link, contentDescription = null, tint = TextSecondary)
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("broadcast_server2_input"),
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = SurfaceVariantDark,
              unfocusedContainerColor = SurfaceVariantDark,
              focusedBorderColor = BroadcastCyan,
              unfocusedBorderColor = Color.Transparent,
              focusedTextColor = TextPrimary,
              unfocusedTextColor = TextPrimary
            ),
            shape = RoundedCornerShape(12.dp)
          )

          Spacer(modifier = Modifier.height(10.dp))

          // Server 3 (Alternative / Fast)
          Text(
            text = "🛡️ Server 3 Stream Link (Alternative / Fast - Optional)",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
          )
          Spacer(modifier = Modifier.height(4.dp))
          OutlinedTextField(
            value = server3Url,
            onValueChange = { server3Url = it },
            placeholder = { Text("https://example.com/live/server3.m3u8 (Optional)", color = TextMuted, fontSize = 12.sp) },
            leadingIcon = {
              Icon(imageVector = Icons.Default.Link, contentDescription = null, tint = TextSecondary)
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("broadcast_server3_input"),
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = SurfaceVariantDark,
              unfocusedContainerColor = SurfaceVariantDark,
              focusedBorderColor = BroadcastCyan,
              unfocusedBorderColor = Color.Transparent,
              focusedTextColor = TextPrimary,
              unfocusedTextColor = TextPrimary
            ),
            shape = RoundedCornerShape(12.dp)
          )

          Spacer(modifier = Modifier.height(12.dp))

          // Sport Category Selection
          Text(
            text = "Sport Category",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
          )
          Spacer(modifier = Modifier.height(6.dp))
          FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            SportCategory.entries.forEach { sport ->
              val isSelected = selectedSport == sport
              Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (isSelected) BroadcastCyan else SurfaceVariantDark,
                modifier = Modifier.clickable { selectedSport = sport }
              ) {
                Text(
                  text = "${sport.icon} ${sport.displayName}",
                  color = if (isSelected) Color.Black else TextSecondary,
                  fontSize = 11.sp,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                  modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          // Match Status
          Text(
            text = "Match Status",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
          )
          Spacer(modifier = Modifier.height(6.dp))
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(MatchStatus.LIVE to "🔴 LIVE NOW", MatchStatus.UPCOMING to "⏰ UPCOMING").forEach { (status, label) ->
              val isSelected = selectedStatus == status
              Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (isSelected) (if (status == MatchStatus.LIVE) LiveRed else Color(0xFFF59E0B)) else SurfaceVariantDark,
                modifier = Modifier.clickable { selectedStatus = status }
              ) {
                Text(
                  text = label,
                  color = if (isSelected) Color.White else TextSecondary,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          // Match Title
          Text(
            text = "Match Title *",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
          )
          Spacer(modifier = Modifier.height(4.dp))
          OutlinedTextField(
            value = streamTitle,
            onValueChange = {
              streamTitle = it
              errorText = null
            },
            placeholder = { Text("e.g. Nepal vs Netherlands, Real Madrid vs Barcelona", color = TextMuted, fontSize = 12.sp) },
            leadingIcon = {
              Icon(imageVector = Icons.Default.Sports, contentDescription = null, tint = TextSecondary)
            },
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("broadcast_stream_title_input"),
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = SurfaceVariantDark,
              unfocusedContainerColor = SurfaceVariantDark,
              focusedBorderColor = BroadcastCyan,
              unfocusedBorderColor = Color.Transparent,
              focusedTextColor = TextPrimary,
              unfocusedTextColor = TextPrimary
            ),
            shape = RoundedCornerShape(12.dp)
          )

          Spacer(modifier = Modifier.height(10.dp))

          // Live Score
          Text(
            text = "Score (Optional)",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
          )
          Spacer(modifier = Modifier.height(4.dp))
          OutlinedTextField(
            value = score,
            onValueChange = { score = it },
            placeholder = { Text("e.g. NEP 156/4 (17.2) or RMA 2 - 1 BAR (74')", color = TextMuted, fontSize = 12.sp) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = SurfaceVariantDark,
              unfocusedContainerColor = SurfaceVariantDark,
              focusedBorderColor = BroadcastCyan,
              unfocusedBorderColor = Color.Transparent,
              focusedTextColor = TextPrimary,
              unfocusedTextColor = TextPrimary
            ),
            shape = RoundedCornerShape(12.dp)
          )

          Spacer(modifier = Modifier.height(12.dp))

          // Notification Checkbox
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .fillMaxWidth()
              .clickable { notifyInstantly = !notifyInstantly }
              .padding(vertical = 4.dp)
          ) {
            Checkbox(
              checked = notifyInstantly,
              onCheckedChange = { notifyInstantly = it },
              colors = CheckboxDefaults.colors(checkedColor = LiveRed)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Column {
              Text(
                text = "Send Instant Push Notification to Viewers",
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                color = TextPrimary
              )
              Text(
                text = "Notify all active devices immediately about the match",
                fontSize = 10.sp,
                color = TextSecondary
              )
            }
          }

          // Auto-play Checkbox
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .fillMaxWidth()
              .clickable { autoPlayNow = !autoPlayNow }
              .padding(vertical = 4.dp)
          ) {
            Checkbox(
              checked = autoPlayNow,
              onCheckedChange = { autoPlayNow = it },
              colors = CheckboxDefaults.colors(checkedColor = BroadcastCyan)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Column {
              Text(
                text = "Auto-Play on Home Immediately",
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                color = TextPrimary
              )
              Text(
                text = "Set as the primary live stream right away on the Home screen",
                fontSize = 10.sp,
                color = TextSecondary
              )
            }
          }

          if (errorText != null) {
            Text(
              text = errorText ?: "",
              color = LiveRed,
              fontSize = 12.sp,
              modifier = Modifier.padding(top = 6.dp)
            )
          }

          if (successFeedback != null) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(top = 8.dp)
            ) {
              Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Success", tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(text = successFeedback ?: "", color = Color(0xFF10B981), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          Button(
            onClick = {
              if (server1Url.isBlank()) {
                errorText = "Please enter Server 1 URL"
                return@Button
              }
              if (streamTitle.isBlank()) {
                errorText = "Please enter a match title"
                return@Button
              }
              val currentEditingId = editingStreamId
              if (currentEditingId != null) {
                val original = allStreams.find { it.id == currentEditingId }
                val updated = LiveStream(
                  id = currentEditingId,
                  title = streamTitle.trim(),
                  streamUrl = server1Url.trim(),
                  sportCategory = selectedSport,
                  matchStatus = selectedStatus,
                  broadcasterName = broadcasterName.ifBlank { "IP TV Broadcaster" },
                  description = description.ifBlank { "${selectedSport.displayName} Match broadcast" },
                  score = score.trim(),
                  server1Url = server1Url.trim(),
                  server2Url = server2Url.trim(),
                  server3Url = server3Url.trim(),
                  isCustomUserStream = true,
                  thumbnailUrl = original?.thumbnailUrl ?: "https://images.unsplash.com/photo-1574629810360-7efbbe195018?w=800&q=80",
                  viewersCount = original?.viewersCount ?: 12000
                )
                onUpdateStream(updated)
                if (autoPlayNow) {
                  onSelectStream(updated)
                }
                editingStreamId = null
                streamTitle = ""
                server1Url = ""
                server2Url = ""
                server3Url = ""
                score = ""
                description = ""
                errorText = null
                successFeedback = "Match stream updated successfully!"
              } else {
                onSaveAndBroadcast(
                  streamTitle,
                  server1Url,
                  selectedSport,
                  selectedStatus,
                  broadcasterName,
                  description,
                  score,
                  server1Url,
                  server2Url,
                  server3Url,
                  notifyInstantly,
                  autoPlayNow
                )
                successFeedback = "Match with 3 Server choices saved & broadcasted! Push alert sent."
                streamTitle = ""
                server1Url = ""
                server2Url = ""
                server3Url = ""
                score = ""
                description = ""
                errorText = null
              }
            },
            colors = ButtonDefaults.buttonColors(containerColor = if (editingStreamId != null) BroadcastCyan else LiveRed),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
              .testTag("broadcast_studio_submit_button")
          ) {
            Icon(
              imageVector = if (editingStreamId != null) Icons.Default.Edit else Icons.Default.Campaign,
              contentDescription = if (editingStreamId != null) "Update" else "Broadcast",
              tint = if (editingStreamId != null) Color.Black else Color.White
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = if (editingStreamId != null) "Update Match Stream 💾" else "Save & Broadcast to All Viewers 🚀",
              color = if (editingStreamId != null) Color.Black else Color.White,
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp
            )
          }
        }
      }
    }

    // Requirement 6: Google AdSense / AdMob Configuration Section
    item {
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = SurfaceDark,
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.MonetizationOn, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Column {
              Text(
                text = "Google AdSense & AdMob Control",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = TextPrimary
              )
              Text(
                text = "Configure publisher IDs and customize ad display formats",
                fontSize = 11.sp,
                color = TextSecondary
              )
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          // Publisher ID
          Text("Google AdSense / AdMob App ID", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = BroadcastCyan)
          Spacer(modifier = Modifier.height(3.dp))
          OutlinedTextField(
            value = pubId,
            onValueChange = { pubId = it },
            placeholder = { Text("ca-app-pub-3940256099942544~3347511713", color = TextMuted, fontSize = 11.sp) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("adsense_pub_id_input"),
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = SurfaceVariantDark,
              unfocusedContainerColor = SurfaceVariantDark,
              focusedBorderColor = BroadcastCyan,
              unfocusedBorderColor = Color.Transparent,
              focusedTextColor = TextPrimary,
              unfocusedTextColor = TextPrimary
            ),
            shape = RoundedCornerShape(10.dp)
          )

          Spacer(modifier = Modifier.height(8.dp))

          // Mandatory Popup Ad Unit ID
          Text("Mandatory 15s Popup Ad Unit ID", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
          Spacer(modifier = Modifier.height(3.dp))
          OutlinedTextField(
            value = interstitialId,
            onValueChange = { interstitialId = it },
            placeholder = { Text("ca-app-pub-3940256099942544/1033173712", color = TextMuted, fontSize = 11.sp) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("adsense_interstitial_id_input"),
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = SurfaceVariantDark,
              unfocusedContainerColor = SurfaceVariantDark,
              focusedBorderColor = BroadcastCyan,
              unfocusedBorderColor = Color.Transparent,
              focusedTextColor = TextPrimary,
              unfocusedTextColor = TextPrimary
            ),
            shape = RoundedCornerShape(10.dp)
          )

          Spacer(modifier = Modifier.height(8.dp))

          // Rewarded 20s Ad Unit ID
          Text("Rewarded 20s Ad Unit ID (Ad-Free Pass)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
          Spacer(modifier = Modifier.height(3.dp))
          OutlinedTextField(
            value = rewardedId,
            onValueChange = { rewardedId = it },
            placeholder = { Text("ca-app-pub-3940256099942544/5224354917", color = TextMuted, fontSize = 11.sp) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("adsense_rewarded_id_input"),
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = SurfaceVariantDark,
              unfocusedContainerColor = SurfaceVariantDark,
              focusedBorderColor = BroadcastCyan,
              unfocusedBorderColor = Color.Transparent,
              focusedTextColor = TextPrimary,
              unfocusedTextColor = TextPrimary
            ),
            shape = RoundedCornerShape(10.dp)
          )

          Spacer(modifier = Modifier.height(8.dp))

          // Rotation Ad Unit ID
          Text("Rotation / Fullscreen Ad Unit ID", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
          Spacer(modifier = Modifier.height(3.dp))
          OutlinedTextField(
            value = rotationId,
            onValueChange = { rotationId = it },
            placeholder = { Text("ca-app-pub-3940256099942544/8691691433", color = TextMuted, fontSize = 11.sp) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("adsense_rotation_id_input"),
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = SurfaceVariantDark,
              unfocusedContainerColor = SurfaceVariantDark,
              focusedBorderColor = BroadcastCyan,
              unfocusedBorderColor = Color.Transparent,
              focusedTextColor = TextPrimary,
              unfocusedTextColor = TextPrimary
            ),
            shape = RoundedCornerShape(10.dp)
          )

          Spacer(modifier = Modifier.height(8.dp))

          // Authorized Website / Domain (Origin & Referrer for Google AdSense verification)
          Text("Authorized Domain / Website URL (AdSense Origin)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = BroadcastCyan)
          Spacer(modifier = Modifier.height(3.dp))
          OutlinedTextField(
            value = authDomain,
            onValueChange = { authDomain = it },
            placeholder = { Text("https://ram6sham.github.io", color = TextMuted, fontSize = 11.sp) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("adsense_auth_domain_input"),
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = SurfaceVariantDark,
              unfocusedContainerColor = SurfaceVariantDark,
              focusedBorderColor = BroadcastCyan,
              unfocusedBorderColor = Color.Transparent,
              focusedTextColor = TextPrimary,
              unfocusedTextColor = TextPrimary
            ),
            shape = RoundedCornerShape(10.dp)
          )

          Surface(
            shape = RoundedCornerShape(8.dp),
            color = SurfaceHighlight,
            modifier = Modifier.fillMaxWidth().padding(top = 6.dp)
          ) {
            Column(modifier = Modifier.padding(10.dp)) {
              Text(
                text = "ℹ️ Why AdSense Ads require a Domain:",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFF59E0B)
              )
              Text(
                text = "Google AdSense validates that ads originate from an approved site added to your AdSense account under 'Sites'. Once your domain or account is active, live Google ad creatives will load automatically.",
                fontSize = 10.sp,
                color = TextSecondary,
                modifier = Modifier.padding(top = 2.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          // Toggles for 3/4 ad ways
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text("1. Mandatory 15s Popup Ad", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
              Text("Initial 15-second mandatory ad, grants 12 hours ad-free afterwards", fontSize = 10.sp, color = TextSecondary)
            }
            Switch(
              checked = popupEnabled,
              onCheckedChange = { popupEnabled = it },
              colors = SwitchDefaults.colors(checkedThumbColor = LiveRed, checkedTrackColor = LiveRed.copy(alpha = 0.5f))
            )
          }

          Spacer(modifier = Modifier.height(8.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text("2. Mobile Rotation Ad", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
              Text("Displays interstitial ad when user rotates mobile to landscape", fontSize = 10.sp, color = TextSecondary)
            }
            Switch(
              checked = rotationEnabled,
              onCheckedChange = { rotationEnabled = it },
              colors = SwitchDefaults.colors(checkedThumbColor = BroadcastCyan, checkedTrackColor = BroadcastCyan.copy(alpha = 0.5f))
            )
          }

          Spacer(modifier = Modifier.height(8.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text("3. In-App Banner Ads", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
              Text("Displays banner ad slot directly beneath the video player", fontSize = 10.sp, color = TextSecondary)
            }
            Switch(
              checked = bannerEnabled,
              onCheckedChange = { bannerEnabled = it },
              colors = SwitchDefaults.colors(checkedThumbColor = BroadcastCyan, checkedTrackColor = BroadcastCyan.copy(alpha = 0.5f))
            )
          }

          if (adConfigSavedFeedback != null) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(top = 10.dp)
            ) {
              Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(adConfigSavedFeedback ?: "", color = Color(0xFF10B981), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          Button(
            onClick = {
              onSaveAdSenseConfig(
                pubId,
                bannerId,
                interstitialId,
                rewardedId,
                rotationId,
                authDomain,
                popupEnabled,
                rotationEnabled,
                bannerEnabled
              )
              adConfigSavedFeedback = "Google AdSense configuration saved successfully!"
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth().testTag("save_adsense_config_button")
          ) {
            Text("Save AdSense Configuration 💾", color = Color.Black, fontWeight = FontWeight.Bold)
          }
        }
      }
    }

    // Active Matches List with Delete Capability
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Manage Active Match Streams (${allStreams.size})",
          fontWeight = FontWeight.Bold,
          fontSize = 15.sp,
          color = TextPrimary
        )
        Text(
          text = "Tap Delete to remove",
          fontSize = 11.sp,
          color = TextMuted
        )
      }
    }

    if (allStreams.isEmpty()) {
      item {
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = SurfaceDark,
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text(
              text = "No active match streams available.",
              color = TextSecondary,
              fontSize = 13.sp
            )
            Text(
              text = "Broadcast a new match above with Server 1, Server 2 & Server 3, or sync with GitHub Gist.",
              color = TextMuted,
              fontSize = 11.sp,
              modifier = Modifier.padding(top = 4.dp)
            )
          }
        }
      }
    } else {
      val uniqueStreams = allStreams.distinctBy { it.id }
      items(uniqueStreams, key = { "admin_${it.id}" }) { stream ->
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = SurfaceDark,
          modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelectStream(stream) }
        ) {
          Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                  shape = RoundedCornerShape(4.dp),
                  color = if (stream.isLive) LiveRed else Color(0xFFF59E0B)
                ) {
                  Text(
                    text = if (stream.isLive) "LIVE" else "UPCOMING",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 9.sp,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                  )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = stream.title,
                  fontWeight = FontWeight.Bold,
                  fontSize = 14.sp,
                  color = TextPrimary,
                  maxLines = 1
                )
              }
              Text(
                text = "${stream.sportCategory.displayName} • S1: ${stream.server1Url}",
                fontSize = 11.sp,
                color = BroadcastCyan,
                maxLines = 1,
                modifier = Modifier.padding(top = 2.dp)
              )
              if (stream.server2Url.isNotBlank()) {
                Text(
                  text = "S2: ${stream.server2Url}",
                  fontSize = 10.sp,
                  color = TextSecondary,
                  maxLines = 1
                )
              }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
              Surface(
                shape = RoundedCornerShape(16.dp),
                color = BroadcastCyan,
                modifier = Modifier
                  .padding(start = 6.dp)
                  .clickable { onSelectStream(stream) }
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                  Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Watch",
                    tint = Color.Black,
                    modifier = Modifier.size(14.dp)
                  )
                  Spacer(modifier = Modifier.width(2.dp))
                  Text("Watch", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
              }

              Surface(
                shape = RoundedCornerShape(16.dp),
                color = BroadcastCyan.copy(alpha = 0.2f),
                modifier = Modifier
                  .padding(start = 6.dp)
                  .clickable {
                    editingStreamId = stream.id
                    streamTitle = stream.title
                    server1Url = stream.server1Url.ifBlank { stream.streamUrl }
                    server2Url = stream.server2Url
                    server3Url = stream.server3Url
                    selectedSport = stream.sportCategory
                    selectedStatus = stream.matchStatus
                    score = stream.score
                    description = stream.description
                    errorText = null
                    successFeedback = "Loaded \"${stream.title}\" for editing above."
                  }
                  .testTag("edit_stream_button_${stream.id}")
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp)
                ) {
                  Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = BroadcastCyan,
                    modifier = Modifier.size(14.dp)
                  )
                  Spacer(modifier = Modifier.width(2.dp))
                  Text("Edit", color = BroadcastCyan, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
              }

              Surface(
                shape = RoundedCornerShape(16.dp),
                color = LiveRed.copy(alpha = 0.18f),
                modifier = Modifier
                  .padding(start = 6.dp)
                  .clickable { streamToDelete = stream }
                  .testTag("delete_stream_button_${stream.id}")
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp)
                ) {
                  Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = LiveRed,
                    modifier = Modifier.size(14.dp)
                  )
                  Spacer(modifier = Modifier.width(2.dp))
                  Text("Delete", color = LiveRed, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
              }
            }
          }
        }
      }
    }
  }

  // Confirmation dialog for Admin Delete Match
  if (streamToDelete != null) {
    AlertDialog(
      onDismissRequest = { streamToDelete = null },
      title = {
        Text("Delete Match Stream?", fontWeight = FontWeight.Bold, color = TextPrimary)
      },
      text = {
        Text(
          text = "Are you sure you want to permanently delete \"${streamToDelete?.title}\"? Once deleted, this stream will be removed immediately and will not reappear.",
          color = TextSecondary,
          fontSize = 13.sp
        )
      },
      confirmButton = {
        Button(
          onClick = {
            streamToDelete?.let { onDeleteStream(it.id) }
            streamToDelete = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = LiveRed),
          shape = RoundedCornerShape(8.dp)
        ) {
          Text("Delete Permanently", color = Color.White, fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { streamToDelete = null }) {
          Text("Cancel", color = TextSecondary)
        }
      },
      containerColor = SurfaceDark
    )
  }
}
