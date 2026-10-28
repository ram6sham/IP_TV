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
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.example.model.StreamCategory
import com.example.ui.theme.BroadcastCyan
import com.example.ui.theme.LiveRed
import com.example.ui.theme.ObsidianBackground
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceHighlight
import com.example.ui.theme.SurfaceVariantDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BroadcastStudioScreen(
  allStreams: List<LiveStream>,
  onSaveAndBroadcast: (
    title: String,
    streamUrl: String,
    category: StreamCategory,
    broadcasterName: String,
    description: String,
    notifyInstantly: Boolean,
    autoPlayNow: Boolean
  ) -> Unit,
  onSelectStream: (LiveStream) -> Unit,
  onOpenRemoteConfig: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  var streamTitle by remember { mutableStateOf("") }
  var streamUrl by remember { mutableStateOf("") }
  var broadcasterName by remember { mutableStateOf("PP TV Broadcaster") }
  var description by remember { mutableStateOf("") }
  var selectedCategory by remember { mutableStateOf(StreamCategory.NEWS) }
  var notifyInstantly by remember { mutableStateOf(true) }
  var autoPlayNow by remember { mutableStateOf(true) }
  var errorText by remember { mutableStateOf<String?>(null) }
  var successFeedback by remember { mutableStateOf<String?>(null) }

  val presetTestLinks = listOf(
    "Mux HLS 1080p" to "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8",
    "Bunny Live Arena" to "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
    "Tears of Steel HD" to "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4"
  )

  val customBroadcasts = allStreams.filter { it.isCustomUserStream }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(ObsidianBackground)
      .testTag("broadcast_studio_screen"),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Top Broadcast Banner
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
                contentDescription = "Broadcast Studio",
                tint = LiveRed,
                modifier = Modifier.size(28.dp)
              )
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = "PP TV Broadcast Studio",
                  fontWeight = FontWeight.Bold,
                  fontSize = 18.sp,
                  color = TextPrimary
                )
                Text(
                  text = "Live Stream Publisher & Instant Notification Center",
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
                text = "⚡ Auto Sync URL",
                color = BroadcastCyan,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
              )
            }
          }

          Text(
            text = "Streem Link राखेर सेभ गर्ने वित्तिकै सबैको मोबाइलमा रहेको एपमा अटो हेर्न मिल्नेछ र तत्कालै नोटिफिकेसन जानेछ।",
            fontSize = 12.sp,
            color = TextSecondary,
            modifier = Modifier.padding(top = 10.dp)
          )
        }
      }
    }

    // Input Form Card
    item {
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = SurfaceDark,
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Text(
            text = "Live Stream Details",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = TextPrimary
          )

          Spacer(modifier = Modifier.height(12.dp))

          // Stream Link Input
          Text(
            text = "Stream Link / URL (HLS .m3u8, MP4)",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = BroadcastCyan
          )
          Spacer(modifier = Modifier.height(4.dp))
          OutlinedTextField(
            value = streamUrl,
            onValueChange = {
              streamUrl = it
              errorText = null
              successFeedback = null
            },
            placeholder = { Text("https://your-domain.com/live/playlist.m3u8", color = TextMuted, fontSize = 12.sp) },
            leadingIcon = {
              Icon(imageVector = Icons.Default.Link, contentDescription = "Link", tint = BroadcastCyan)
            },
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("broadcast_stream_url_input"),
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

          // Quick Preset Test Links
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(top = 6.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            presetTestLinks.forEach { (label, url) ->
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = SurfaceHighlight,
                modifier = Modifier
                  .clickable {
                    streamUrl = url
                    if (streamTitle.isBlank()) streamTitle = "Live Broadcast: $label"
                  }
              ) {
                Text(
                  text = label,
                  fontSize = 11.sp,
                  color = TextSecondary,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
              }
            }
          }

          // Stream Title
          Text(
            text = "Stream Title / कार्यक्रमको शीर्षक",
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
            placeholder = { Text("e.g. Nepal Live Festival, Election News, Sports", color = TextMuted, fontSize = 12.sp) },
            leadingIcon = {
              Icon(imageVector = Icons.Default.Tv, contentDescription = "Title", tint = TextSecondary)
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

          Spacer(modifier = Modifier.height(12.dp))

          // Broadcaster Name
          Text(
            text = "Broadcaster / Organization Name",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
          )
          Spacer(modifier = Modifier.height(4.dp))
          OutlinedTextField(
            value = broadcasterName,
            onValueChange = { broadcasterName = it },
            placeholder = { Text("PP TV Official Studio", color = TextMuted, fontSize = 12.sp) },
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

          // Category Chips
          Text(
            text = "Category",
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
            StreamCategory.entries.filter { it != StreamCategory.ALL }.forEach { category ->
              val isSelected = selectedCategory == category
              Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (isSelected) BroadcastCyan else SurfaceVariantDark,
                modifier = Modifier.clickable { selectedCategory = category }
              ) {
                Text(
                  text = category.displayName,
                  color = if (isSelected) Color.Black else TextSecondary,
                  fontSize = 11.sp,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                  modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Instant Notification Checkbox
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
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.NotificationsActive,
                  contentDescription = "Notification",
                  tint = LiveRed,
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "Send Instant Push Notification to Viewers",
                  fontWeight = FontWeight.SemiBold,
                  fontSize = 12.sp,
                  color = TextPrimary
                )
              }
              Text(
                text = "प्रयोगकर्ताहरूलाई तत्कालै नोटिफिकेसन जाने सुविधा",
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
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.PlayCircle,
                  contentDescription = "Auto-play",
                  tint = BroadcastCyan,
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "Auto-Play on Everyone's App Immediately",
                  fontWeight = FontWeight.SemiBold,
                  fontSize = 12.sp,
                  color = TextPrimary
                )
              }
              Text(
                text = "सेभ गर्ने बित्तिकै तुरुन्तै स्क्रिनमा लाइभ प्ले हुने",
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

          // Broadcast Button
          Button(
            onClick = {
              if (streamUrl.isBlank()) {
                errorText = "Please enter a valid Stream Link (URL)"
                return@Button
              }
              if (streamTitle.isBlank()) {
                errorText = "Please enter a stream title"
                return@Button
              }
              onSaveAndBroadcast(
                streamTitle,
                streamUrl,
                selectedCategory,
                broadcasterName,
                description,
                notifyInstantly,
                autoPlayNow
              )
              successFeedback = "Stream published & broadcasted successfully! Notification dispatched."
            },
            colors = ButtonDefaults.buttonColors(containerColor = LiveRed),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
              .testTag("broadcast_studio_submit_button")
          ) {
            Icon(
              imageVector = Icons.Default.Campaign,
              contentDescription = "Broadcast",
              tint = Color.White
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Save & Broadcast to All Viewers 🚀",
              color = Color.White,
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp
            )
          }
        }
      }
    }

    // Active Custom Broadcasts List
    item {
      Text(
        text = "Your Broadcasted Stream Links (${customBroadcasts.size})",
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp,
        color = TextPrimary
      )
    }

    if (customBroadcasts.isEmpty()) {
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
              text = "No custom stream links published yet.",
              color = TextSecondary,
              fontSize = 13.sp
            )
            Text(
              text = "Fill the form above with your stream link to broadcast live to everyone!",
              color = TextMuted,
              fontSize = 11.sp,
              modifier = Modifier.padding(top = 4.dp)
            )
          }
        }
      }
    } else {
      items(customBroadcasts) { stream ->
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
                  color = LiveRed
                ) {
                  Text(
                    text = "LIVE",
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
                text = stream.streamUrl,
                fontSize = 11.sp,
                color = BroadcastCyan,
                maxLines = 1,
                modifier = Modifier.padding(top = 2.dp)
              )
              Text(
                text = "${stream.category.displayName} • ${stream.broadcasterName}",
                fontSize = 11.sp,
                color = TextSecondary,
                modifier = Modifier.padding(top = 2.dp)
              )
            }

            Surface(
              shape = RoundedCornerShape(16.dp),
              color = BroadcastCyan,
              modifier = Modifier.padding(start = 10.dp)
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
          }
        }
      }
    }
  }
}
