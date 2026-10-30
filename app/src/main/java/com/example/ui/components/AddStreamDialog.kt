package com.example.ui.components

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
import androidx.compose.material.icons.filled.Sports
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
import com.example.model.MatchStatus
import com.example.model.SportCategory
import com.example.ui.theme.BroadcastCyan
import com.example.ui.theme.LiveRed
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceHighlight
import com.example.ui.theme.SurfaceVariantDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

/**
 * Requirement 3: Add Match with Server 1, Server 2, and Server 3 options.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddStreamDialog(
  onDismiss: () -> Unit,
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
  ) -> Unit
) {
  var streamTitle by remember { mutableStateOf("") }
  var server1Url by remember { mutableStateOf("") }
  var server2Url by remember { mutableStateOf("") }
  var server3Url by remember { mutableStateOf("") }
  var broadcasterName by remember { mutableStateOf("IP TV Sports") }
  var description by remember { mutableStateOf("") }
  var score by remember { mutableStateOf("") }
  var selectedCategory by remember { mutableStateOf(SportCategory.CRICKET) }
  var selectedStatus by remember { mutableStateOf(MatchStatus.LIVE) }
  var notifyInstantly by remember { mutableStateOf(true) }
  var autoPlayNow by remember { mutableStateOf(true) }

  var errorMessage by remember { mutableStateOf<String?>(null) }

  val presetLinks = listOf(
    "Mux HLS 1080p" to "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8",
    "Bunny Live Arena" to "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
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
              contentDescription = "Admin Broadcast",
              tint = LiveRed,
              modifier = Modifier.size(26.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Add Match & Multi-Servers",
              fontWeight = FontWeight.Bold,
              fontSize = 17.sp,
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
          text = "Configure Server 1, Server 2, and Server 3 streaming links. Viewers will be able to switch between servers instantly.",
          fontSize = 11.sp,
          color = TextSecondary,
          modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
        )

        // Server 1 (Primary)
        Text(
          text = "⚡ Server 1 URL (Primary Stream) *",
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          color = BroadcastCyan
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
          value = server1Url,
          onValueChange = {
            server1Url = it
            errorMessage = null
          },
          placeholder = { Text("https://example.com/live/server1.m3u8", color = TextMuted, fontSize = 12.sp) },
          leadingIcon = {
            Icon(imageVector = Icons.Default.Link, contentDescription = "Server 1", tint = BroadcastCyan)
          },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("server1_url_input"),
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

        // Presets helper
        Row(
          modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 8.dp),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          presetLinks.forEach { (label, url) ->
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = SurfaceHighlight,
              modifier = Modifier.clickable {
                server1Url = url
                if (streamTitle.isBlank()) streamTitle = "Live Sports: $label"
              }
            ) {
              Text(text = label, fontSize = 10.sp, color = TextSecondary, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
            }
          }
        }

        // Server 2 (Backup / HD)
        Text(
          text = "🚀 Server 2 URL (Backup / HD - Optional)",
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
            Icon(imageVector = Icons.Default.Link, contentDescription = "Server 2", tint = TextSecondary)
          },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("server2_url_input"),
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
          text = "🛡️ Server 3 URL (Alternative / Fast - Optional)",
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
            Icon(imageVector = Icons.Default.Link, contentDescription = "Server 3", tint = TextSecondary)
          },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("server3_url_input"),
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

        // Sport Category Selector
        Text("Sport Category", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
        Spacer(modifier = Modifier.height(6.dp))
        FlowRow(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          SportCategory.entries.forEach { sport ->
            val isSelected = selectedCategory == sport
            Surface(
              shape = RoundedCornerShape(20.dp),
              color = if (isSelected) BroadcastCyan else SurfaceVariantDark,
              modifier = Modifier.clickable { selectedCategory = sport }
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

        // Match Status (LIVE / UPCOMING)
        Text("Match Status", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
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
        Text("Match Title *", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
          value = streamTitle,
          onValueChange = {
            streamTitle = it
            errorMessage = null
          },
          placeholder = { Text("e.g. Nepal vs Netherlands, Real Madrid vs Barcelona", color = TextMuted, fontSize = 12.sp) },
          leadingIcon = {
            Icon(Icons.Default.Sports, contentDescription = null, tint = TextSecondary)
          },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("stream_title_input"),
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

        // Live Score (Optional)
        Text("Live Score (Optional)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
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
          modifier = Modifier.fillMaxWidth().clickable { notifyInstantly = !notifyInstantly }.padding(vertical = 2.dp)
        ) {
          Checkbox(
            checked = notifyInstantly,
            onCheckedChange = { notifyInstantly = it },
            colors = CheckboxDefaults.colors(checkedColor = LiveRed)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(text = "Send instant notification to all viewers", fontSize = 11.sp, color = TextPrimary)
        }

        // Auto-play Checkbox
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.fillMaxWidth().clickable { autoPlayNow = !autoPlayNow }.padding(vertical = 2.dp)
        ) {
          Checkbox(
            checked = autoPlayNow,
            onCheckedChange = { autoPlayNow = it },
            colors = CheckboxDefaults.colors(checkedColor = BroadcastCyan)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(text = "Auto-play on Home screen immediately after saving", fontSize = 11.sp, color = TextPrimary)
        }

        if (errorMessage != null) {
          Text(text = errorMessage ?: "", color = LiveRed, fontSize = 12.sp, modifier = Modifier.padding(top = 6.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

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
              if (server1Url.isBlank()) {
                errorMessage = "Please enter Server 1 URL"
                return@Button
              }
              if (streamTitle.isBlank()) {
                errorMessage = "Please enter a match title"
                return@Button
              }
              onSaveAndBroadcast(
                streamTitle,
                server1Url,
                selectedCategory,
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
            },
            colors = ButtonDefaults.buttonColors(containerColor = LiveRed),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.testTag("save_and_broadcast_button")
          ) {
            Text("Save & Broadcast 🚀", color = Color.White, fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}
