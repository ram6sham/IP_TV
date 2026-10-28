package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.StreamCategory
import com.example.ui.theme.BroadcastCyan
import com.example.ui.theme.LiveRed
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceHighlight
import com.example.ui.theme.SurfaceVariantDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddStreamDialog(
  onDismiss: () -> Unit,
  onSaveAndBroadcast: (
    title: String,
    streamUrl: String,
    category: StreamCategory,
    broadcasterName: String,
    description: String,
    notifyInstantly: Boolean,
    autoPlayNow: Boolean
  ) -> Unit
) {
  var streamTitle by remember { mutableStateOf("") }
  var streamUrl by remember { mutableStateOf("") }
  var broadcasterName by remember { mutableStateOf("PP TV Live") }
  var description by remember { mutableStateOf("") }
  var selectedCategory by remember { mutableStateOf(StreamCategory.NEWS) }
  var notifyInstantly by remember { mutableStateOf(true) }
  var autoPlayNow by remember { mutableStateOf(true) }

  var errorMessage by remember { mutableStateOf<String?>(null) }

  // Quick Preset Samples for immediate testing
  val presetLinks = listOf(
    "Mux HLS Live" to "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8",
    "Big Buck HD" to "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
    "Tears of Steel" to "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
    "Blender Sintel" to "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4"
  )

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      shape = RoundedCornerShape(18.dp),
      color = SurfaceDark,
      modifier = Modifier
        .fillMaxWidth(0.92f)
        .padding(vertical = 16.dp)
        .testTag("add_stream_dialog")
    ) {
      Column(
        modifier = Modifier
          .padding(20.dp)
          .verticalScroll(rememberScrollState())
      ) {
        // Top Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Campaign,
              contentDescription = "Broadcast",
              tint = LiveRed,
              modifier = Modifier.size(26.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Broadcast Live Stream",
              fontWeight = FontWeight.Bold,
              fontSize = 18.sp,
              color = TextPrimary
            )
          }

          IconButton(
            onClick = onDismiss,
            modifier = Modifier.size(32.dp).testTag("close_add_stream_dialog")
          ) {
            Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
          }
        }

        Text(
          text = "Streem Link राखेर सेभ गर्नुहोस् — तुरुन्तै सबैको मोबाइलमा अटो प्ले हुनेछ र तत्कालै नोटिफिकेसन जानेछ।",
          fontSize = 12.sp,
          color = TextSecondary,
          modifier = Modifier.padding(top = 4.dp, bottom = 14.dp)
        )

        // Stream URL Input
        Text(
          text = "Stream Link / URL (HLS .m3u8 or MP4)",
          fontSize = 12.sp,
          fontWeight = FontWeight.SemiBold,
          color = BroadcastCyan
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
          value = streamUrl,
          onValueChange = {
            streamUrl = it
            errorMessage = null
          },
          placeholder = { Text("https://example.com/live/stream.m3u8", color = TextMuted, fontSize = 12.sp) },
          leadingIcon = {
            Icon(imageVector = Icons.Default.Link, contentDescription = "Link", tint = BroadcastCyan)
          },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("stream_url_input"),
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

        // Quick sample test links
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp, bottom = 12.dp),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          presetLinks.forEach { (label, url) ->
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = SurfaceHighlight,
              modifier = Modifier
                .clickable {
                  streamUrl = url
                  if (streamTitle.isBlank()) streamTitle = "Live Stream: $label"
                }
            ) {
              Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = TextSecondary,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          }
        }

        // Stream Title
        Text(
          text = "Stream Title / कार्यक्रमको नाम",
          fontSize = 12.sp,
          fontWeight = FontWeight.SemiBold,
          color = TextPrimary
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
          value = streamTitle,
          onValueChange = {
            streamTitle = it
            errorMessage = null
          },
          placeholder = { Text("e.g. PP TV Breaking Live or Sports Match", color = TextMuted, fontSize = 12.sp) },
          leadingIcon = {
            Icon(imageVector = Icons.Default.Tv, contentDescription = "Title", tint = TextSecondary)
          },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("stream_title_input"),
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
          text = "Broadcaster / Channel Name",
          fontSize = 12.sp,
          fontWeight = FontWeight.SemiBold,
          color = TextPrimary
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
          value = broadcasterName,
          onValueChange = { broadcasterName = it },
          placeholder = { Text("PP TV Studio / Host Name", color = TextMuted, fontSize = 12.sp) },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("broadcaster_name_input"),
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

        // Category Selection
        Text(
          text = "Select Category",
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
                contentDescription = "Alert",
                tint = LiveRed,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "Instant Push Notification to Users",
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                color = TextPrimary
              )
            }
            Text(
              text = "तत्कालै सबै प्रयोगकर्ताहरूलाई अलर्ट सूचना पठाउनुहोस्",
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
                text = "Auto-Play on Everyone's App",
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                color = TextPrimary
              )
            }
            Text(
              text = "सेभ गर्ने बित्तिकै तुरुन्तै स्क्रिनमा लाइभ प्ले गर्नुहोस्",
              fontSize = 10.sp,
              color = TextSecondary
            )
          }
        }

        if (errorMessage != null) {
          Text(
            text = errorMessage ?: "",
            color = LiveRed,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 8.dp)
          )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Action Buttons
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.End,
          verticalAlignment = Alignment.CenterVertically
        ) {
          TextButton(onClick = onDismiss) {
            Text("Cancel", color = TextSecondary)
          }

          Spacer(modifier = Modifier.width(8.dp))

          Button(
            onClick = {
              if (streamUrl.isBlank()) {
                errorMessage = "Please enter a valid Stream Link (URL)"
                return@Button
              }
              if (streamTitle.isBlank()) {
                errorMessage = "Please enter a stream title"
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
            },
            colors = ButtonDefaults.buttonColors(containerColor = LiveRed),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.testTag("save_and_broadcast_button")
          ) {
            Icon(
              imageVector = Icons.Default.Campaign,
              contentDescription = "Save & Broadcast",
              tint = Color.White,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Save & Broadcast 🚀",
              color = Color.White,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    }
  }
}
